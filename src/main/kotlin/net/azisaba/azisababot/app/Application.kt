package net.azisaba.azisababot.app

import dev.kord.core.Kord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.azisaba.azisababot.app.command.*
import net.azisaba.azisababot.config
import net.azisaba.azisababot.logger

lateinit var kord: Kord

private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

suspend fun app() {
    logger.info("Starting bot...")

    kord = Kord(System.getenv(config.app.tokenEnv))
    kord.login {
        kord.guilds.collect { guild ->
            abmAddServerCommand(guild)
            abmRemoveServerCommand(guild)
            abmServerListCommand(guild)
            abmAddServerToGroupCommand(guild)
            abmRemoveServerFromGroupCommand(guild)

            abmCreateGroupCommand(guild)
            abmDeleteGroupCommand(guild)
            abmGroupListCommand(guild)

            abmAddEndpointCommand(guild)
            abmRemoveEndpointCommand(guild)
            abmEndpointListCommand(guild)

            abmCreateScheduleCommand(guild)
            abmDeleteGroupCommand(guild)
            abmScheduleListCommand(guild)
        }

        abmAddServerCommand(kord)
        abmRemoveServerCommand(kord)
        abmServerListCommand(kord)
        abmAddServerToGroupCommand(kord)
        abmRemoveServerFromGroupCommand(kord)

        abmCreateGroupCommand(kord)
        abmDeleteGroupCommand(kord)
        abmGroupListCommand(kord)

        abmAddEndpointCommand(kord)
        abmRemoveEndpointCommand(kord)
        abmEndpointListCommand(kord)

        abmCreateScheduleCommand(kord)
        abmDeleteGroupCommand(kord)
        abmScheduleListCommand(kord)

        logger.info("Bot started")
    }
}

fun updateServerCommands() {
    coroutineScope.launch {
        kord.guilds.collect { guild ->
            abmAddServerCommand(guild)
            abmRemoveServerCommand(guild)
            abmAddServerToGroupCommand(guild)

            abmAddEndpointCommand(guild)
            abmEndpointListCommand(guild)
        }
    }
}

fun updateServerGroupCommands() {
    coroutineScope.launch {
        kord.guilds.collect { guild ->
            abmServerListCommand(guild)
            abmAddServerToGroupCommand(guild)
            abmRemoveServerFromGroupCommand(guild)

            abmDeleteGroupCommand(guild)

            abmCreateScheduleCommand(guild)
        }
    }
}