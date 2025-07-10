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

suspend fun serverEditCommand(guild: Guild) = guild.createChatInputCommand("server-edit", "Edit any Minecraft server") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "任意の Minecraft サーバーを編集します"
    )

    string("server", "Server to edit") {
        required = true
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "編集するサーバー"
        )
    }

    string("server-id", "New server ID") {
        required = false
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "新しいサーバーID"
        )
    }

    string("display-name", "New display name") {
        required = false
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "新しい表示名"
        )
    }
}

fun serverEditCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == "server-edit" } ?: return@on

    val serverId = command.strings["server"]!!
    val server = Server.server(serverId)

    if (server == null) {
        interaction.respondEphemeral {
            content = ":x: `${serverId}` は無効なサーバーIDです"
        }
        return@on
    }

    val newServerId = command.strings["server-id"]
    val newDisplayName = command.strings["display-name"]

    if ((newServerId == null || newServerId == server.serverId) && (newDisplayName == null || newDisplayName == server.displayName)) {
        interaction.respondEphemeral {
            content = ":warning: 変更はありませんでした"
        }
        return@on
    }

    val response = interaction.deferPublicResponse()

    val stringBuilder = StringBuilder()
    stringBuilder.append(":pencil: ${server.appNotation()} を更新しました")
    stringBuilder.append('\n')
    stringBuilder.append("```diff")

    newServerId?.let { new ->
        val old = server.serverId
        stringBuilder.append('\n')
        try {
            server.serverId = new
            stringBuilder.append("Server ID：")
            stringBuilder.append('\n')
            stringBuilder.append("+ $new")
            stringBuilder.append('\n')
            stringBuilder.append("- $old")
        } catch (e: Exception) {
            stringBuilder.append("Server ID：${e.message}")
        }
    }

    newDisplayName?.let { new ->
        val old = server.displayName
        stringBuilder.append('\n')
        try {
            server.displayName = new
            stringBuilder.append("Display Name：")
            stringBuilder.append('\n')
            stringBuilder.append("+ $new")
            stringBuilder.append('\n')
            stringBuilder.append("- $old")
        } catch (e: Exception) {
            stringBuilder.append("Display Name：${e.message}")
        }
    }

    stringBuilder.append("```")

    response.respond {
        content = stringBuilder.toString()
    }
}