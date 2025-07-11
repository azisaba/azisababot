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

private const val COMMAND_NAME: String = "abm-rename-server"

suspend fun abmRenameServerCommand(guild: Guild) = guild.createChatInputCommand(COMMAND_NAME, "Rename a server") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーの名前を変更します"
    )

    string("server", "Server to rename") {
        required = true
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to  "名前を変更するサーバー"
        )
        for (server in Server.servers()) {
            choice(server.nameOrId, server.id)
        }
    }

    string("new-name", "New name") {
        required = false
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "新しい名前"
        )
    }
}

fun abmRenameServerCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == COMMAND_NAME } ?: return@on

    val serverId = command.strings["server"]!!
    val server = Server.server(serverId)
    if (server == null) {
        interaction.respondEphemeral {
            content = i18n("command.errors.server_not_found", serverId)
        }
        return@on
    }

    val response = interaction.deferPublicResponse()

    val newName = command.strings["new-name"]

    try {
        server.name = newName
        response.respond {
            content = i18n("command.abm_rename_server.success", server.toAppName(), newName)
        }
    } catch (e: Exception) {
        response.respond {
            content = i18n("command.abm_rename_server.failure", e.message)
        }
    }
}