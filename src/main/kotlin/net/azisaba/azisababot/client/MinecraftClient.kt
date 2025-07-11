package net.azisaba.azisababot.client

import net.azisaba.azisababot.config
import net.azisaba.azisababot.server.ServerStatus
import net.azisaba.azisababot.server.Server

interface MinecraftClient {
    val protocolVersion: Int

    val timeout: Int

    suspend fun serverStatus(endpoint: Server.Endpoint): ServerStatus?

    suspend fun ping(endpoint: Server.Endpoint): Long?

    companion object {
        fun client(
            protocolVersion: Int = config.client.protocolVersion,
            timeout: Int = config.client.timeout
        ): MinecraftClient = MinecraftClientImpl(protocolVersion, timeout)
    }
}