package net.azisaba.azisababot.app.command

import dev.kord.common.Locale
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import net.azisaba.azisababot.server.Server
import java.util.*

private val buttonRegex = Regex("""endpoint-list-([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})-(\d+)""")

suspend fun endpointListCommand(guild: Guild) = guild.createChatInputCommand("endpoint-list", "List the endpoints for a server") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーのエンドポイントをリスト表示します"
    )

    string("server", "Server to list") {
        required = true
        maxLength = 16
    }

    integer("page", "Page Number") {
        required = false
        minValue = 1
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "ページ番号"
        )
    }
}

fun endpointListCommand(kord: Kord) {
    kord.on<ChatInputCommandInteractionCreateEvent> {
        val command = interaction.command.takeIf { it.rootName == "endpoint-list" } ?: return@on
        val response = interaction.deferEphemeralResponse()

        val serverId = command.strings["server"]!!
        val server = Server.server(serverId)

        if (server == null) {
            response.respond {
                content = ":x: `${serverId}` は無効なサーバーIDです"
            }
            return@on
        }

        val page = command.integers["page"]?.toInt() ?: 1
        val pageIndex = page - 1

        response.respond {
            createMessage(server, pageIndex, this)
        }
    }

    kord.on<ButtonInteractionCreateEvent> {
        val customId = interaction.componentId
        val match = buttonRegex.find(customId) ?: return@on

        val serverUuid = match.groups[1]?.value?.let { UUID.fromString(it) } ?: return@on
        val server = Server.server(serverUuid) ?: return@on

        val pageIndex = match.groups[2]?.value?.toIntOrNull() ?: return@on

        interaction.updatePublicMessage {
            components = mutableListOf()
            createMessage(server, pageIndex, this)
        }
    }
}

private fun createMessage(server: Server, pageIndex: Int, messageBuilder: MessageBuilder) {
    val endpoints = server.endpoints.chunked(10)

    if (pageIndex !in endpoints.indices) {
        messageBuilder.content = ":warning: 間違った場所に来てしまったようです"
        messageBuilder.actionRow {
            interactionButton(ButtonStyle.Primary, "endpoint-${server.uuid}-0") {
                label = "エンドポイントリストの始めに戻る"
            }
        }
        return
    }

    val stringBuilder = StringBuilder()
    stringBuilder.append(":notepad_spiral: **${server.appNotation()} のエンドポイントリスト (${pageIndex + 1}/${endpoints.size})**")

    for (endpoint in endpoints[pageIndex]) {
        stringBuilder.append('\n')
        stringBuilder.append("- $endpoint")
    }

    stringBuilder.append('\n')
    stringBuilder.append("-# 全${endpoints.flatten().size}件のエンドポイント")

    messageBuilder.content = stringBuilder.toString()

    val previousPageIndex = pageIndex - 1
    val shouldShowPrevious = previousPageIndex in endpoints.indices

    val nextPageIndex = pageIndex + 1
    val shouldShowNext = nextPageIndex in endpoints.indices

    if (shouldShowPrevious || shouldShowNext) {
        messageBuilder.actionRow {
            if (shouldShowPrevious) {
                interactionButton(ButtonStyle.Secondary, "endpoint-list-${server.uuid}-$previousPageIndex") {
                    label = "◀ 前へ"
                }
            }

            if (shouldShowNext) {
                interactionButton(ButtonStyle.Secondary, "endpoints-list-${server.uuid}-$nextPageIndex") {
                    label = "次へ ▶"
                }
            }
        }
    }
}