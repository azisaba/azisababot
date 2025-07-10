package net.azisaba.azisababot.app.command

import dev.kord.common.Locale
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.server.group.ServerGroup

suspend fun serverAddToGroupCommand(guild: Guild) = guild.createChatInputCommand("server-add-to-group", "Add a server to the server group") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーグループにサーバーを追加します"
    )

    string("group", "Server group to add server to") {
        required = true
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "サーバーを追加するサーバーグループ"
        )
    }

    string("server", "Server to be added to the server group") {
        required = true
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "サーバーグループに追加するサーバー"
        )
    }
}

fun serverAddToGroupCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == "server-add-to-group" } ?: return@on

    val groupId = command.strings["group"]!!
    val group = ServerGroup.group(groupId)
    if (group == null) {
        interaction.respondEphemeral {
            content = ":x: `$groupId` は無効なサーバーグループIDです"
        }
        return@on
    }

    val serverId = command.strings["server"]!!
    val server = Server.server(serverId)
    if (server == null) {
        interaction.respondEphemeral {
            content = ":x: $serverId は無効なサーバーIDです"
        }
        return@on
    }

    if (server in group) {
        interaction.respondEphemeral {
            content = ":warning: ${server.appNotation()} は既に ${group.appNotation()} に含まれています"
        }
        return@on
    }

    group += server
    interaction.respondPublic {
        content = ":white_check_mark: ${server.appNotation()} を ${group.appNotation()} に追加しました"
    }
}