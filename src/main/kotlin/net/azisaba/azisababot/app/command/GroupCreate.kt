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

suspend fun groupCreateCommand(guild: Guild) = guild.createChatInputCommand("group-create", "Create a server group") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーグループを作成します"
    )

    string("group-id", "Server group identifier, can contain a-z, 0-9, and underscores") {
        required = true
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "サーバ－グループの識別子 a-z、0-9、アンダーバーが使用できます"
        )
    }

    string("display-name", "The display name of the server group") {
        required = false
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "サーバーグループを表示するときに使用される名前"
        )
    }
}

fun groupCreateCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == "group-create" } ?: return@on

    val groupId = command.strings["group-id"]!!
    val displayName = command.strings["display-name"] ?: groupId

    try {
        val group = ServerGroup.group {
            this.groupId = groupId
            this.displayName = displayName
        }
        interaction.respondPublic {
            content = ":white_check_mark: サーバーグループ ${group.appNotation()} を作成しました"
        }
    } catch (e: IllegalStateException) {
        interaction.respondEphemeral {
            content = ":x: サーバーグループの作成に失敗しました (`${e.message}`)"
        }
    }
}