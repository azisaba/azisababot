package net.azisaba.azisababot.config

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val database: DatabaseConfig
)
