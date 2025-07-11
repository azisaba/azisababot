package net.azisaba.azisababot.app

import dev.kord.core.Kord
import net.azisaba.azisababot.config

suspend fun app() {
    val kord = Kord(System.getenv(config.app.tokenEnv))
    kord.login()
    kord.guilds.collect { guild ->
    }
}