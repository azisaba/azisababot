package net.azisaba.azisababot.app.command

import dev.kord.common.Locale
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.util.i18n

suspend fun abmRemoveServerCommand(guild: Guild) = guild.createChatInputCommand("abm-remove-server", "Remove a server from to crawling") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーをクロールの対象から削除します"
    )

    string("server", "Server to be removed") {
        required = true
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "削除するサーバー"
        )
        for (server in Server.servers()) {
            choice(server.name ?: server.id, server.id)
        }
    }
}

fun abmRemoveServerCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == "abm-remove-server" } ?: return@on

    val serverId = command.strings["server"]!!
    val server = Server.server(serverId)

    if (server == null) {
        interaction.respondEphemeral {
            content = i18n("command.errors.server_not_found", serverId)
        }
        return@on
    }

    val response = interaction.deferPublicResponse()

    try {
        server.remove()
        response.respond {
            content = i18n("command.abm_remove_server.success", server.toAppName())
        }
    } catch (e: Exception) {
        response.respond {
            content = i18n("command.abm_remove_server.failure", e.message)
        }
    }
}