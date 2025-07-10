package net.azisaba.azisababot.app.command.group

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

suspend fun groupDeleteCommand(guild: Guild) = guild.createChatInputCommand("group-delete", "Delete a server group") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーグループを削除します"
    )

    string("group", "Server group to be deleted") {
        required = true
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "削除するサーバーグループ"
        )
    }
}

fun groupDeleteCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == "group-delete" } ?: return@on

    val groupId = command.strings["group-id"]!!
    val group = ServerGroup.group(groupId)

    if (group == null) {
        interaction.respondEphemeral {
            content = ":x: `$groupId` は無効なサーバーグループIDです"
        }
        return@on
    }

    group.remove()

    interaction.respondPublic {
        content = ":white_check_mark: サーバーグループ ${group.appNotation()} を削除しました"
    }
}