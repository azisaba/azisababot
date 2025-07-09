package net.azisaba.azisababot.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.azisaba.azisababot.client.packet.*
import net.azisaba.azisababot.crawler.ServerStatus
import net.azisaba.azisababot.server.Server
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

internal class MinecraftClientImpl(
    override val protocolVersion: Int,
    override var timeout: Int
) : MinecraftClient {
    override suspend fun fetchServerStatus(endpoint: Server.Endpoint): ServerStatus? = withContext(Dispatchers.IO) {
        withSocketConnection(endpoint) { input, output ->
            handshake(endpoint, output)
            Packet.packet(ServerboundStatusRequest).send(output)

            val statusResponse = Packet.packet(ClientboundStatusResponse).apply {
                readFrom(input)
            }
            statusResponse[ClientboundStatusResponse.jsonResponse]?.let { ServerStatus.status(ClientboundStatusResponse.deserialize(it)) }
        }
    }

    override suspend fun fetchPing(endpoint: Server.Endpoint): Long? = withContext(Dispatchers.IO) {
        withSocketConnection(endpoint) { input, output ->
            handshake(endpoint, output)
            Packet.packet(ServerboundPingRequest).apply {
                this[ServerboundPingRequest.timestamp] = System.currentTimeMillis()
            }

            val pongResponse = Packet.packet(ClientboundPongResponse).apply {
                readFrom(input)
            }
            pongResponse[ClientboundPongResponse.timestamp]?.let { System.currentTimeMillis() - it }
        }
    }

    private fun handshake(endpoint: Server.Endpoint, output: OutputStream) {
        Packet.packet(ServerboundHandshake).apply {
            this[ServerboundHandshake.protocolVersion] = protocolVersion
            this[ServerboundHandshake.serverAddress] = endpoint.host
            this[ServerboundHandshake.serverPort] = endpoint.port.toUShort()
            this[ServerboundHandshake.intent] = ServerboundHandshake.Intent.STATUS.id
        }.send(output)
    }

    private fun <T> withSocketConnection(endpoint: Server.Endpoint, block: (InputStream, OutputStream) -> T): T? {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(endpoint.host, endpoint.port), timeout)
                block(socket.getInputStream(), socket.getOutputStream())
            }
        } catch (e: Exception) {
            null
        }
    }
}