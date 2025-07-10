package net.azisaba.azisababot.app.command.server

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

suspend fun serverUpdateCommand(guild: Guild) = guild.createChatInputCommand("server-update", "Update any server") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "任意のサーバーを更新します"
    )

    string("server", "Server to update") {
        required = true
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "更新するサーバー"
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

fun serverUpdateCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == "server-update" } ?: return@on

    val serverId = command.strings["server"]!!
    val server = Server.server(serverId)

    if (server == null) {
        interaction.respondEphemeral {
            content = ":x: `$serverId` は無効なサーバーIDです"
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

    val stringBuilder = StringBuilder()
    stringBuilder.append(":pencil: サーバー ${server.appNotation()} を更新しました")
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
            stringBuilder.append("Display name：")
            stringBuilder.append('\n')
            stringBuilder.append("+ $new")
            stringBuilder.append('\n')
            stringBuilder.append("- $old")
        } catch (e: Exception) {
            stringBuilder.append("Display name：${e.message}")
        }
    }

    stringBuilder.append("```")

    interaction.respondPublic {
        content = stringBuilder.toString()
    }
}