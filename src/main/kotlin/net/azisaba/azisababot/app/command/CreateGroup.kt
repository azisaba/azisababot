package net.azisaba.azisababot.app.command

import dev.kord.common.Locale
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import net.azisaba.azisababot.server.group.ServerGroup
import net.azisaba.azisababot.util.i18n

private const val COMMAND_NAME: String = "abt-create-group"

suspend fun abtCreateGroupCommand(guild: Guild) = guild.createChatInputCommand(COMMAND_NAME, "Create a server group") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーグループを作成します"
    )

    string("id", "Server group identifier, can contain a-z, 0-9, and underscores") {
        required = true
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "サーバーグループのID a-z、0-9、アンダーバーが使用できます"
        )
    }

    string("name", "Server group name, can contain any characters") {
        required = false
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "サーバーグループの名前 任意の文字を使用できます"
        )
    }
}

fun abtCreateGroupCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == COMMAND_NAME } ?: return@on

    val response = interaction.deferPublicResponse()

    val id = command.strings["id"]!!
    val name = command.strings["name"]

    try {
        val group = ServerGroup.group {
            this.id = id
            this.name = name
        }
        response.respond {
            content = i18n("command.abm_create_group.success", group.toAppName())
        }
    } catch (e: Exception) {
        response.respond {
            content = i18n("command.abm_create_group.failure", e.message)
        }
    }
}