package net.azisaba.azisababot.app.command

import dev.kord.common.Locale
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.util.i18n

suspend fun abmAddServerCommand(guild: Guild) = guild.createChatInputCommand("abm-add-server", "Add a server to crawl") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "クロールするサーバーを追加します"
    )

    string("id", "Server identifier, can contain a-z, 0-9, and underscores") {
        required = true
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "サーバーのID a-z、0-9、アンダーバーが使用できます"
        )
    }

    string("name", "Server name, can contain any characters") {
        required = false
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "サーバーの名前 任意の文字を使用できます"
        )
    }
}

fun abmAddServerCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == "abm-add-server" } ?: return@on

    val id = command.strings["id"]!!
    val name = command.strings["name"]

    val response = interaction.deferPublicResponse()

    try {
        val server = Server.server {
            this.id = id
            this.name = name
        }
        response.respond {
            content = i18n("command.abm_add_server.success", server.toAppName())
        }
    } catch (e: Exception) {
        response.respond {
            content = i18n("command.abm_add_server.failure", e.message)
        }
    }
}