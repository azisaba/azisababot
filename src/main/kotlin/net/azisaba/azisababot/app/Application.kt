package net.azisaba.azisababot.app

import dev.kord.core.Kord
import net.azisaba.azisababot.app.command.*
import net.azisaba.azisababot.config

suspend fun app() {
    val kord = Kord(System.getenv(config.app.tokenEnv))
    kord.login {
        kord.guilds.collect { guild ->
            endpointAddCommand(guild)
            endpointEditCommand(guild)
            endpointListCommand(guild)
            endpointRemoveCommand(guild)

            serverAddCommand(guild)
            serverEditCommand(guild)
            serverListCommand(guild)
            serverRemoveCommand(guild)
        }

        endpointAddCommand(kord)
        endpointEditCommand(kord)
        endpointListCommand(kord)
        endpointRemoveCommand(kord)

        serverAddCommand(kord)
        serverEditCommand(kord)
        serverListCommand(kord)
        serverRemoveCommand(kord)
    }
}