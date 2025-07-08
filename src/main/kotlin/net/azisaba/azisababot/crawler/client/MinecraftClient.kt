package net.azisaba.azisababot.crawler.client

import net.azisaba.azisababot.config.ClientConfig
import net.azisaba.azisababot.crawler.client.impl.MinecraftClientImpl
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.server.status.ServerStatus

interface MinecraftClient {
    val protocol: Int

    fun serverStatus(endpoint: Server.Endpoint): ServerStatus?

    fun ping(endpoint: Server.Endpoint): Long?

    companion object {
        fun client(config: ClientConfig = ClientConfig(771, 7000)): MinecraftClient = MinecraftClientImpl(config)
    }
}