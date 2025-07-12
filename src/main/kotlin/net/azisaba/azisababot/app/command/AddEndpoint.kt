package net.azisaba.azisababot.app.command

import dev.kord.common.Locale
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.interaction.string
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.util.i18n

private const val COMMAND_NAME: String = "abt-add-endpoint"

suspend fun abtAddEndpointCommand(guild: Guild) = guild.createChatInputCommand(COMMAND_NAME, "Add an endpoint to a server") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーにエンドポイントを追加します"
    )

    string("server", "Server to added endpoint to") {
        required = true
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "エンドポイントを追加するサーバー"
        )
        for (server in Server.servers()) {
            choice(server.nameOrId, server.id)
        }
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
        minValue = 0
        maxValue = 65535
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "例：25565"
        )
    }

    integer("priority", "The priority of this endpoint") {
        required = false
        minValue = 0
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "このエンドポイントの優先度"
        )
    }
}

fun abtAddEndpointCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == COMMAND_NAME } ?: return@on

    val serverId = command.strings["server"]!!
    val server = Server.server(serverId)

    if (server == null) {
        interaction.respondEphemeral {
            content = i18n("command.errors.server_not_found", serverId)
        }
        return@on
    }

    val response = interaction.deferPublicResponse()

    val host = command.strings["host"]!!
    val port = command.integers["port"]?.toInt() ?: 25565
    val endpoint = Server.Endpoint.of(host, port)

    val priority = command.integers["priority"]?.toInt() ?: 0

    server[endpoint] = priority

    response.respond {
        content = i18n("command.abm_add_endpoint.success", server.toAppName(), endpoint, priority)
    }
}