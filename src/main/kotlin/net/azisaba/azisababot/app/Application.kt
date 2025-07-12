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
            abtAddServerCommand(guild)
            abtRemoveServerCommand(guild)
            abtServerListCommand(guild)
            abtRenameServerCommand(guild)
            abtAddServerToGroupCommand(guild)
            abtRemoveServerFromGroupCommand(guild)

            abtCreateGroupCommand(guild)
            abtDeleteGroupCommand(guild)
            abtRenameGroupCommand(guild)
            abtGroupListCommand(guild)

            abtAddEndpointCommand(guild)
            abtRemoveEndpointCommand(guild)
            abtEndpointListCommand(guild)

            abtCreateScheduleCommand(guild)
            abtDeleteGroupCommand(guild)
            abtScheduleListCommand(guild)

            abtLineGraphCommand(guild)
        }

        abtAddServerCommand(kord)
        abtRemoveServerCommand(kord)
        abtServerListCommand(kord)
        abtRenameServerCommand(kord)
        abtAddServerToGroupCommand(kord)
        abtRemoveServerFromGroupCommand(kord)

        abtCreateGroupCommand(kord)
        abtDeleteGroupCommand(kord)
        abtGroupListCommand(kord)
        abtRenameGroupCommand(kord)

        abtAddEndpointCommand(kord)
        abtRemoveEndpointCommand(kord)
        abtEndpointListCommand(kord)

        abtCreateScheduleCommand(kord)
        abtScheduleListCommand(kord)

        abtLineGraphCommand(kord)

        logger.info("Bot started")
    }
}

fun updateServerCommands() {
    coroutineScope.launch {
        kord.guilds.collect { guild ->
            abtAddServerCommand(guild)
            abtRemoveServerCommand(guild)
            abtRenameServerCommand(guild)
            abtAddServerToGroupCommand(guild)

            abtAddEndpointCommand(guild)
            abtEndpointListCommand(guild)
        }
    }
}

fun updateServerGroupCommands() {
    coroutineScope.launch {
        kord.guilds.collect { guild ->
            abtServerListCommand(guild)
            abtAddServerToGroupCommand(guild)
            abtRemoveServerFromGroupCommand(guild)

            abtDeleteGroupCommand(guild)
            abtRenameGroupCommand(guild)

            abtCreateScheduleCommand(guild)
        }
    }
}