package net.azisaba.azisababot.app.command.endpoint

import dev.kord.common.Locale
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.interaction.string
import net.azisaba.azisababot.server.Server

suspend fun endpointAddCommand(guild: Guild) = guild.createChatInputCommand("endpoint-add", "Add an endpoint to a server") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーにエンドポイントを追加します"
    )

    string("server", "Server to add endpoint to") {
        required = true
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "エンドポイントを追加するサーバー"
        )
    }

    string("host", "e.g. mc.azisaba.net") {
        required = true
        maxLength = 255
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "例：mc.azisaba.net"
        )
    }

    integer("port", "e.g. 25565") {
        required = false
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "例：25565"
        )
        minValue = 0
        maxValue = 65535
    }

    integer("priority", "The priority of this endpoint") {
        required = false
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "このエンドポイントの優先度"
        )
        minValue = 0
    }
}

fun endpointAddCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == "endpoint-add" } ?: return@on
    val response = interaction.deferPublicResponse()

    val serverId = command.strings["server"]!!
    val server = Server.server(serverId)

    if (server == null) {
        response.respond {
            content = ":x: `${serverId}` は無効なサーバーIDです"
        }
        return@on
    }

    val host = command.strings["host"]!!
    val port = command.integers["port"]?.toInt() ?: 25565
    val priority = command.integers["priority"]?.toInt()
    val endpoint = Server.Endpoint.of(host, port)

    if (priority != null) {
        server.endpoints[priority] = endpoint
    } else {
        server.endpoints += endpoint
    }

    response.respond {
        content = ":white_check_mark: ${server.appNotation()} にエンドポイント `$endpoint` を追加しました"
    }
}