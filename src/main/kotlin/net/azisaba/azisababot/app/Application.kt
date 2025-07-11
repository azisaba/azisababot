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
            abmAddEndpointCommand(guild)
            abmAddServerCommand(guild)

            abmAddServerToGroupCommand(guild)

            abmCreateGroupCommand(guild)
            abmDeleteGroupCommand(guild)

            abmEndpointListCommand(guild)

            abmGroupListCommand(guild)

            abmRemoveEndpointCommand(guild)
            abmRemoveServerCommand(guild)

            abmRemoveServerFromGroupCommand(guild)

            abmServerListCommand(guild)
        }

        abmAddEndpointCommand(kord)
        abmAddServerCommand(kord)

        abmAddServerToGroupCommand(kord)

        abmCreateGroupCommand(kord)
        abmDeleteGroupCommand(kord)

        abmEndpointListCommand(kord)

        abmGroupListCommand(kord)

        abmRemoveEndpointCommand(kord)
        abmRemoveServerCommand(kord)

        abmRemoveServerFromGroupCommand(kord)

        abmServerListCommand(kord)

        logger.info("Bot started")
    }
}

fun updateServerCommands() {
    coroutineScope.launch {
        kord.guilds.collect { guild ->
            abmAddEndpointCommand(guild)
            abmAddServerCommand(guild)
            abmAddServerToGroupCommand(guild)
            abmEndpointListCommand(guild)
            abmEndpointListCommand(guild)
            abmRemoveServerCommand(guild)
        }
    }
}

fun updateServerGroupCommands() {
    coroutineScope.launch {
        kord.guilds.collect { guild ->
            abmAddServerToGroupCommand(guild)
            abmDeleteGroupCommand(guild)
            abmRemoveServerFromGroupCommand(guild)
            abmServerListCommand(guild)
        }
    }
}