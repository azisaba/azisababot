package net.azisaba.azisababot.crawler.client.impl

import net.azisaba.azisababot.config.ClientConfig
import net.azisaba.azisababot.crawler.client.MinecraftClient
import net.azisaba.azisababot.packet.Packet
import net.azisaba.azisababot.packet.ServerboundHandshakePacket
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.server.status.ServerStatus
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

internal class MinecraftClientImpl(private val config: ClientConfig) : MinecraftClient {
    override val protocol: Int = config.protocol

    override fun serverStatus(endpoint: Server.Endpoint): ServerStatus? = connect(endpoint) { input, output ->
        handshake(endpoint, output)
        Packet.serverboundStatusRequest().send(output)

        val statusResponse = Packet.clientboundStatusResponse().apply { readFrom(input) }
        statusResponse.jsonResponse?.let { ServerStatus.status(it) }
    }

    override fun ping(endpoint: Server.Endpoint): Long? = connect(endpoint) { input, output ->
        handshake(endpoint, output)
        Packet.serverboundPingRequest().send(output)

        val pongResponse = Packet.clientboundPongResponse().apply { readFrom(input) }
        pongResponse.timestamp?.let { System.currentTimeMillis() - it }
    }

    private fun handshake(endpoint: Server.Endpoint, output: OutputStream) {
        Packet.serverboundHandshake().apply {
            protocolVersion = protocol
            serverAddress = endpoint.host
            serverPort = endpoint.port.toShort()
            intent = ServerboundHandshakePacket.Intent.STATUS
        }.send(output)
    }

    private fun <T> connect(endpoint: Server.Endpoint, block: (InputStream, OutputStream) -> T): T? {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(endpoint.host, endpoint.port), config.timeout)
                val output = socket.getOutputStream()
                val input = socket.getInputStream()
                block(input, output)
            }
        } catch (e: IOException) {
            null
        }
    }
}