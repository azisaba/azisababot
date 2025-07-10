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

            groupCreateCommand(guild)
            groupDeleteCommand(guild)
            groupListCommand(guild)
            groupUpdateCommand(guild)

            serverAddCommand(guild)
            serverAddToGroupCommand(guild)
            serverListCommand(guild)
            serverRemoveCommand(guild)
            serverRemoveFromGroupCommand(guild)
            serverUpdateCommand(guild)
        }

        endpointAddCommand(kord)
        endpointEditCommand(kord)
        endpointListCommand(kord)
        endpointRemoveCommand(kord)

        groupCreateCommand(kord)
        groupDeleteCommand(kord)
        groupListCommand(kord)
        groupUpdateCommand(kord)

        serverAddCommand(kord)
        serverAddToGroupCommand(kord)
        serverListCommand(kord)
        serverRemoveCommand(kord)
        serverRemoveFromGroupCommand(kord)
        serverUpdateCommand(kord)
    }
}