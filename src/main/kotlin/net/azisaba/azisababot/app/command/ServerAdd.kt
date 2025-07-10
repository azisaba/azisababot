package net.azisaba.azisababot.app.command

import dev.kord.common.Locale
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import net.azisaba.azisababot.server.Server

suspend fun serverAddCommand(guild: Guild) = guild.createChatInputCommand("server-add", "Add a Minecraft server to crawl") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "クロールする Minecraft サーバーを追加します"
    )

    string("server-id", "Server identifier, can contain a-z, 0-9, and underscores") {
        required = true
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "サーバーの識別子 a-z、0-9、アンダーバーが使用できます"
        )
    }

    string("display-name", "The display name of the server") {
        required = false
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "サーバーを表示するときに使用される名前"
        )
    }
}

fun serverAddCommand(kord: Kord) = kord.on<GuildChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == "server-add" } ?: return@on
    val response = interaction.deferPublicResponse()

    val serverId = command.strings["server-id"]!!
    val displayName = command.strings["display-name"] ?: serverId

    try {
        val server = Server.server {
            this.serverId = serverId
            this.displayName = displayName
        }
        response.respond {
            content = ":white_check_mark: サーバー ${server.appNotation()} を追加しました"
        }
    } catch (e: IllegalStateException) {
        response.respond {
            content = ":x: サーバーの追加に失敗しました (`${e.message}`)"
        }
    }
}