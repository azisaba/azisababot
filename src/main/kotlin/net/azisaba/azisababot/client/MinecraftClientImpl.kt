package net.azisaba.azisababot.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.azisaba.azisababot.client.packet.*
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.server.ServerStatus
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

internal class MinecraftClientImpl(
    override val protocolVersion: Int,
    override var timeout: Int
) : MinecraftClient {
    override suspend fun serverStatus(endpoint: Server.Endpoint): ServerStatus? = withContext(Dispatchers.IO) {
        withSocketConnection(endpoint) { input, output ->
            handshake(endpoint, output)
            Packet.packet(ServerboundStatusRequest).send(output)
            val statusResponsePacket = Packet.packet(ClientboundStatusResponse, input)
            return@withSocketConnection statusResponsePacket[ClientboundStatusResponse.jsonResponse]?.let { ServerStatus.status(it) }
        }
    }

    override suspend fun ping(endpoint: Server.Endpoint): Long? = withContext(Dispatchers.IO) {
        withSocketConnection(endpoint) { input, output ->
            handshake(endpoint, output)
            Packet.packet(ServerboundPingRequest) {
                this[ServerboundPingRequest.timestamp] = System.currentTimeMillis()
            }.send(output)
            val pongResponsePacket = Packet.packet(ClientboundPongResponse, input)
            return@withSocketConnection pongResponsePacket[ClientboundPongResponse.timestamp]?.let { System.currentTimeMillis() - it }
        }
    }

    private fun handshake(endpoint: Server.Endpoint, output: OutputStream) {
        Packet.packet(ServerboundHandshake) {
            this[ServerboundHandshake.protocolVersion] = protocolVersion
            this[ServerboundHandshake.serverAddress] = endpoint.host
            this[ServerboundHandshake.serverPort] = endpoint.port.toUShort()
            this[ServerboundHandshake.intent] = ServerboundHandshake.Intent.STATUS
        }.send(output)
    }

    private fun <T> withSocketConnection(endpoint: Server.Endpoint, block: (InputStream, OutputStream) -> T): T? {
        return try {
            Socket().use { socket ->
                socket.connect(endpoint.toINetSocketAddress(), timeout)
                block(socket.getInputStream(), socket.getOutputStream())
            }
        } catch (e: Exception) {
            null
        }
    }
}