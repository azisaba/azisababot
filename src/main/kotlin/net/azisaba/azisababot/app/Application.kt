package net.azisaba.azisababot.app

import dev.kord.core.Kord
import net.azisaba.azisababot.app.command.endpoint.endpointAddCommand
import net.azisaba.azisababot.app.command.endpoint.endpointEditCommand
import net.azisaba.azisababot.app.command.endpoint.endpointListCommand
import net.azisaba.azisababot.app.command.endpoint.endpointRemoveCommand
import net.azisaba.azisababot.app.command.group.groupCreateCommand
import net.azisaba.azisababot.app.command.group.groupDeleteCommand
import net.azisaba.azisababot.app.command.group.groupListCommand
import net.azisaba.azisababot.app.command.group.groupUpdateCommand
import net.azisaba.azisababot.app.command.schedule.scheduleCreateCommand
import net.azisaba.azisababot.app.command.schedule.scheduleDeleteCommand
import net.azisaba.azisababot.app.command.schedule.scheduleListCommand
import net.azisaba.azisababot.app.command.server.*
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

            scheduleCreateCommand(guild)
            scheduleDeleteCommand(guild)
            scheduleListCommand(guild)

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

        scheduleCreateCommand(kord)
        scheduleDeleteCommand(kord)
        scheduleListCommand(kord)

        serverAddCommand(kord)
        serverAddToGroupCommand(kord)
        serverListCommand(kord)
        serverRemoveCommand(kord)
        serverRemoveFromGroupCommand(kord)
        serverUpdateCommand(kord)
    }
}