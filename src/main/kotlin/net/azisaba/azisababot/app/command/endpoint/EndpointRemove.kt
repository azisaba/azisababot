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

suspend fun endpointRemoveCommand(guild: Guild) = guild.createChatInputCommand("endpoint-remove", "Remove an endpoint from a server") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "エンドポイントをサーバーから削除します"
    )

    string("server", "Server to delete endpoint") {
        required = true
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "エンドポイントを削除するサーバー"
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
        minValue = 0
        maxValue = 65535
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "例：25565"
        )
    }
}

fun endpointRemoveCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == "endpoint-remove" } ?: return@on
    val response = interaction.deferPublicResponse()

    val serverId = command.strings["server"]!!
    val server = Server.server(serverId)

    if (server == null) {
        response.respond {
            content = ":x: `${serverId}` は無効なサーバーIDです"
        }
        return@on
    }

    val host = command.strings["host"]
    val port = command.integers["port"]?.toInt()

    val condition = "host=${host ?: "*"}, port=${port ?: "*"}"

    val matchedEndpoints = server.endpoints.filter {
        (host == null || it.host == host) || (port == null || it.port == port)
    }

    if (matchedEndpoints.isEmpty()) {
        response.respond {
            content = ":warning: ${server.appNotation()} に `$condition` と一致するエンドポイントが見つからなかったため、変更は行われませんでした"
        }
        return@on
    }

    server.endpoints.removeAll(matchedEndpoints)

    val stringBuilder = StringBuilder()
    stringBuilder.append(":white_check_mark: ${server.appNotation()} から `$condition` に一致するエンドポイントを削除しました")
    for (endpoint in matchedEndpoints) {
        stringBuilder.append('\n')
        stringBuilder.append("- $endpoint")
    }
    stringBuilder.append('\n')
    stringBuilder.append("-# 全${matchedEndpoints.size}件のエンドポイントを削除")

    response.respond {
        content = stringBuilder.toString()
    }
}