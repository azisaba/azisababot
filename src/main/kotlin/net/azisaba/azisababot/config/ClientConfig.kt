package net.azisaba.azisababot.config

import kotlinx.serialization.Serializable

@Serializable
data class ClientConfig(
    val protocolVersion: Int,
    val timeout: Int
)
