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
import net.azisaba.azisababot.server.group.ServerGroup
import net.azisaba.azisababot.util.i18n

private const val COMMAND_NAME: String = "abt-add-server-to-group"

suspend fun abtAddServerToGroupCommand(guild: Guild) = guild.createChatInputCommand(COMMAND_NAME, "Add a server to a server group") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーグループにサーバーを追加します"
    )

    string("group", "Server group to add server to") {
        required = true
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "サーバーを追加するサーバーグループ"
        )
        for (group in ServerGroup.groups()) {
            choice(group.nameOrId, group.id)
        }
    }

    string("server", "Server to be added to the server group") {
        required = true
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "サーバーグループに追加するサーバー"
        )
        for (server in Server.servers()) {
            choice(server.nameOrId, server.id)
        }
    }
}

fun abtAddServerToGroupCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == COMMAND_NAME } ?: return@on

    val groupId = command.strings["group"]!!
    val group = ServerGroup.group(groupId)
    if (group == null) {
        interaction.respondEphemeral {
            content = i18n("command.errors.group_not_found", groupId)
        }
        return@on
    }

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
        group += server
        response.respond {
            content = i18n("command.abm_add_server_to_group.success", server.toAppName(), group.toAppName())
        }
    } catch (e: Exception) {
        response.respond {
            content = i18n("command.abm_add_server_to_group.failure", e.message)
        }
    }
}