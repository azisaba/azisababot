package net.azisaba.azisababot.config

import kotlinx.serialization.Serializable

@Serializable
data class ClientConfig(
    val protocol: Int,
    val timeout: Int
)
