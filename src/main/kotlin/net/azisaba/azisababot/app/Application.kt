package net.azisaba.azisababot.app

import dev.kord.core.Kord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.azisaba.azisababot.config
import net.azisaba.azisababot.logger

lateinit var kord: Kord

private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

suspend fun app() {
    kord = Kord(System.getenv(config.app.tokenEnv))
    kord.login {
        kord.guilds.collect { guild ->
            abmAddServerCommand(guild)
            abmRemoveServerCommand(guild)

            abmServerListCommand(guild)
        }

        abmAddServerCommand(kord)
        abmRemoveServerCommand(kord)

        abmServerListCommand(kord)

        logger.info("Bot started")
    }
}

fun updateServerCommands() {
    coroutineScope.launch {
        kord.guilds.collect { guild ->
            abmAddServerCommand(guild)
            abmRemoveServerCommand(guild)
        }
    }
}