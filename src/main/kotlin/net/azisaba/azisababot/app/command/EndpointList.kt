package net.azisaba.azisababot.app.command

import dev.kord.common.Locale
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.util.i18n

private const val COMMAND_NAME: String = "abt-endpoint-list"

private const val ENDPOINT_PER_PAGE: Int = 10

private val buttonRegex: Regex = Regex(""""endpoint-list-(${Server.ID_REGEX.pattern.removePrefix("^").removeSuffix("$")})-(\d+)"""")

suspend fun abtEndpointListCommand(guild: Guild) = guild.createChatInputCommand(COMMAND_NAME, "List the endpoints of a server") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーのエンドポイントをリスト表示します"
    )

    string("server", "Server to list") {
        required = true
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "リスト表示するサーバー"
        )
        for (server in Server.servers()) {
            choice(server.nameOrId, server.id)
        }
    }

    integer("page", "Page index") {
        required = false
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "ページのインデックス"
        )
    }
}

fun abtEndpointListCommand(kord: Kord) {
    kord.on<ChatInputCommandInteractionCreateEvent> {
        val command = interaction.command.takeIf { it.rootName == COMMAND_NAME } ?: return@on
        val response = interaction.deferEphemeralResponse()

        val serverId = command.strings["server"]!!
        val server = Server.server(serverId)

        if (server == null) {
            response.respond {
                content = i18n("command.errors.server_not_found", serverId)
            }
            return@on
        }

        val page = command.integers["page"]?.toInt() ?: 0

        response.respond {
            buildPage(page, server, this)
        }
    }
}

private fun buildPage(page: Int, server: Server, builder: MessageBuilder) {
    val endpoints = server.toList().chunked(ENDPOINT_PER_PAGE)

    if (endpoints.isEmpty()) {
        builder.content = i18n("command.abm_endpoint_list.empty")
        return
    }

    if (page !in endpoints.indices) {
        builder.content = i18n("command.abm_endpoint_list.invalid_page")
        return
    }

    val stringBuilder = StringBuilder()

    stringBuilder.append("**")
    stringBuilder.append(i18n("command.abm_endpoint_list.list.title", page + 1, endpoints.size))
    stringBuilder.append("**")

    for ((priority, endpoint) in endpoints[page]) {
        stringBuilder.append('\n')
        stringBuilder.append("- ")
        stringBuilder.append(i18n("command.abm_endpoint_list.list.element", endpoint, priority))
    }

    stringBuilder.append('\n')
    stringBuilder.append("-# ")
    stringBuilder.append(i18n("command.abm_endpoint_list.list.footer", endpoints.flatten().size))

    builder.content = stringBuilder.toString()

    val previousPage = page - 1
    val shouldShowPrevious = previousPage in endpoints.indices

    val nextPage = page + 1
    val shouldShowNext = nextPage in endpoints.indices

    if (shouldShowPrevious || shouldShowNext) {
        builder.actionRow {
            if (shouldShowPrevious) {
                interactionButton(ButtonStyle.Secondary, "endpoint-list-${server.id}-$previousPage") {
                    label = i18n("command.abm_endpoint_list.list.previous")
                }
            }

            if (shouldShowNext) {
                interactionButton(ButtonStyle.Secondary, "endpoint-list-${server.id}-$nextPage") {
                    label = i18n("command.abm_endpoint_list.list.next")
                }
            }
        }
    }
}