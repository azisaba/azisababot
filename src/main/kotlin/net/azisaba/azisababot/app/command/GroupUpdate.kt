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
import net.azisaba.azisababot.server.group.ServerGroup

suspend fun groupUpdateCommand(guild: Guild) = guild.createChatInputCommand("group-update", "Update any server group") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "任意のサーバーグループを更新します"
    )

    string("group", "Server group to update") {
        required = true
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "更新するサーバーグループ"
        )
    }

    string("group-id", "New server group id") {
        required = false
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "新しいサーバーグループID"
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

fun groupUpdateCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == "group-update" } ?: return@on

    val groupId = command.strings["group"]!!
    val group = ServerGroup.group(groupId)

    if (group == null) {
        interaction.respondEphemeral {
            content = ":x: `$groupId` は無効なサーバーグループIDです"
        }
        return@on
    }

    val newGroupId = command.strings["group-id"]
    val newDisplayName = command.strings["display-name"]

    if ((newGroupId == null || newGroupId == group.groupId) && (newDisplayName == null || newDisplayName == group.displayName)) {
        interaction.respondEphemeral {
            content = ":warning: 変更はありませんでした"
        }
        return@on
    }

    val stringBuilder = StringBuilder()
    stringBuilder.append(":pencil: サーバーグループ ${group.appNotation()} を更新しました")
    stringBuilder.append('\n')
    stringBuilder.append("```diff")

    newGroupId?.let { new ->
        val old = group.groupId
        stringBuilder.append('\n')
        try {
            group.groupId = new
            stringBuilder.append("Server Group ID：")
            stringBuilder.append("\n")
            stringBuilder.append("+ $new")
            stringBuilder.append('\n')
            stringBuilder.append("- $old")
        } catch (e: Exception) {
            stringBuilder.append("Server Group ID：${e.message}")
        }
    }

    newDisplayName?.let { new ->
        val old = group.displayName
        stringBuilder.append('\n')
        try {
            group.displayName = new
            stringBuilder.append("Display name：")
            stringBuilder.append('\n')
            stringBuilder.append("+ $new")
            stringBuilder.append('\n')
            stringBuilder.append("- $old")
        } catch (e: Exception) {
            stringBuilder.append("Display name: ${e.message}")
        }
    }

    stringBuilder.append("```")

    interaction.respondPublic {
        content = stringBuilder.toString()
    }
}