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
import net.azisaba.azisababot.server.group.ServerGroup
import net.azisaba.azisababot.util.i18n

private const val COMMAND_NAME: String = "abt-rename-group"

suspend fun abtRenameGroupCommand(guild: Guild) = guild.createChatInputCommand(COMMAND_NAME, "Rename a server group") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーグループの名前を変更します"
    )

    string("group", "Server group to rename") {
        required = true
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "名前を変更するサーバーグループ"
        )
        for (group in ServerGroup.groups()) {
            choice(group.nameOrId, group.id)
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

fun abtRenameGroupCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == COMMAND_NAME } ?: return@on

    val groupId = command.strings["group"]!!
    val group = ServerGroup.group(groupId)
    if (group == null) {
        interaction.respondEphemeral {
            content = i18n("command.errors.group_not_found", groupId)
        }
        return@on
    }

    val response = interaction.deferPublicResponse()

    val newName = command.strings["new-name"]

    try {
        group.name = newName
        response.respond {
            content = i18n("command.abm_rename_group.success", group.toAppName(), newName)
        }
    } catch (e: Exception) {
        response.respond {
            content = i18n("command.abm_rename_group.failure", e.message)
        }
    }
}