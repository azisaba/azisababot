package net.azisaba.azisababot.client

import net.azisaba.azisababot.server.status.ServerStatus
import net.azisaba.azisababot.server.Server

interface MinecraftClient {
    val protocolVersion: Int

    var timeout: Int

    suspend fun fetchServerStatus(endpoint: Server.Endpoint): ServerStatus?

    suspend fun fetchPing(endpoint: Server.Endpoint): Long?

    companion object {
        fun client(protocolVersion: Int = 772, timeout: Int = 7000): MinecraftClient = MinecraftClientImpl(protocolVersion, timeout)
    }
}