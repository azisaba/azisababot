package net.azisaba.azisababot.config

import kotlinx.serialization.Serializable
import net.azisaba.azisababot.util.LocaleSerializer
import java.util.Locale

@Serializable
data class Config(
    val app: AppConfig,
    val client: ClientConfig,
    val database: DatabaseConfig,
    @Serializable(with = LocaleSerializer::class) val locale: Locale
)
