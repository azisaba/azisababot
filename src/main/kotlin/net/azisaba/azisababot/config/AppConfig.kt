package net.azisaba.azisababot.config

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val tokenEnv: String
)
