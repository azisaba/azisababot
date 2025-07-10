package net.azisaba.azisababot.config

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val app: AppConfig,
    val client: ClientConfig,
    val database: DatabaseConfig
)
