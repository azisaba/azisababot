package net.azisaba.azisababot.app.command

import dev.kord.common.Locale
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import net.azisaba.azisababot.server.Server

private val servers: List<List<Server>>
    get() = Server.servers().chunked(10)

private val buttonRegex: Regex = Regex("""server-list-(\d+)""")

suspend fun serverListCommand(guild: Guild) = guild.createChatInputCommand("server-list", "List Minecraft servers to crawl") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "クロールする Minecraft サーバーをリスト表示します"
    )

    integer("page", "Page Number") {
        required = false
        minValue = 1
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "ページ番号"
        )
    }
}

fun serverListCommand(kord: Kord) {
    kord.on<GuildChatInputCommandInteractionCreateEvent> {
        val command = interaction.command.takeIf { it.rootName == "server-list" } ?: return@on
        val response = interaction.deferEphemeralResponse()

        val page = command.integers["page"]?.toInt() ?: 1
        val pageIndex = page - 1

        response.respond {
            createMessage(pageIndex, this)
        }
    }

    kord.on<ButtonInteractionCreateEvent> {
        val customId = interaction.componentId
        val match = buttonRegex.find(customId) ?: return@on

        val pageIndex = match.groups[1]?.value?.toIntOrNull() ?: return@on

        interaction.updatePublicMessage {
            components = mutableListOf()
            createMessage(pageIndex, this)
        }
    }
}

private fun createMessage(pageIndex: Int, messageBuilder: MessageBuilder) {
    if (pageIndex !in servers.indices) {
        messageBuilder.content = ":warning: 間違った場所に来てしまったようです"
        messageBuilder.actionRow {
            interactionButton(ButtonStyle.Primary, "server-list-0") {
                label = "サーバーリストの始めに戻る"
            }
        }
        return
    }

    val stringBuilder = StringBuilder()
    stringBuilder.append(":notepad_spiral: **サーバーリスト (${pageIndex + 1}/${servers.size})**")

    for (server in servers[pageIndex]) {
        stringBuilder.append('\n')
        stringBuilder.append("- ${server.appNotation()}")
    }

    stringBuilder.append('\n')
    stringBuilder.append("-# 全${servers.flatten().size}件のサーバー")

    messageBuilder.content = stringBuilder.toString()

    val previousPageIndex = pageIndex - 1
    val shouldShowPrevious = previousPageIndex in servers.indices

    val nextPageIndex = pageIndex + 1
    val shouldShowNext = nextPageIndex in servers.indices

    if (shouldShowPrevious || shouldShowNext) {
        messageBuilder.actionRow {
            if (shouldShowPrevious) {
                interactionButton(ButtonStyle.Secondary, "server-list-$previousPageIndex") {
                    label = "◀ 前へ"
                }
            }

            if (shouldShowNext) {
                interactionButton(ButtonStyle.Secondary, "server-list-$nextPageIndex") {
                    label = "次へ ▶"
                }
            }
        }
    }
}