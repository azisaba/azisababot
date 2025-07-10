package net.azisaba.azisababot.app

import dev.kord.core.Kord
import net.azisaba.azisababot.app.command.serverAddCommand
import net.azisaba.azisababot.app.command.serverListCommand
import net.azisaba.azisababot.app.command.serverRemoveCommand
import net.azisaba.azisababot.config

suspend fun app() {
    val kord = Kord(System.getenv(config.app.tokenEnv))
    kord.login {
        kord.guilds.collect { guild ->
            serverAddCommand(guild)
            serverListCommand(guild)
            serverRemoveCommand(guild)
        }

        serverAddCommand(kord)
        serverListCommand(kord)
        serverRemoveCommand(kord)
    }
}