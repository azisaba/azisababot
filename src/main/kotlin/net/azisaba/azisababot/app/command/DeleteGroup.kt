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

private const val COMMAND_NAME: String = "abt-delete-group"

suspend fun abtDeleteGroupCommand(guild: Guild) = guild.createChatInputCommand(COMMAND_NAME, "Delete a server group") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーグループを消去します"
    )

    string("group", "Server group to be deleted") {
        required = true
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "消去するサーバーグループ"
        )
        for (group in ServerGroup.groups()) {
            choice(group.nameOrId, group.id)
        }
    }
}

fun abtDeleteGroupCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
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

    group.remove()

    response.respond {
        content = i18n("command.abm_delete_group.success", group.toAppName())
    }
}