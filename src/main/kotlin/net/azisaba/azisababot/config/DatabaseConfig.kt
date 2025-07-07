package net.azisaba.azisababot.config

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
    val url: String,
    val usernameEnv: String,
    val passwordEnv: String,
    val maxPoolSize: Int
)
