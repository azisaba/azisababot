package net.azisaba.azisababot.config

import kotlinx.serialization.Serializable

@Serializable
data class CrawlerConfig(
    val maxRetries: Int
)
