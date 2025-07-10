package net.azisaba.azisababot.app.command.server

import dev.kord.common.Locale
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.behavior.interaction.updateEphemeralMessage
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.server.group.ServerGroup
import java.util.*

private val buttonRegex: Regex = Regex("""server-list-([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})-(\d+)""")

private val dummyUuid: UUID = UUID.randomUUID()

suspend fun serverListCommand(guild: Guild) = guild.createChatInputCommand("server-list", "List servers to crawl") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "クロールするサーバーをリスト表示します"
    )

    integer("page", "Page number") {
        required = false
        minValue = 1
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "ページ番号"
        )
    }

    string("group", "Server group") {
        required = false
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "サーバーグループ"
        )
    }
}

fun serverListCommand(kord: Kord) {
    kord.on<GuildChatInputCommandInteractionCreateEvent> {
        val command = interaction.command.takeIf { it.rootName == "server-list" } ?: return@on
        val response = interaction.deferEphemeralResponse()

        val page = command.integers["page"]?.toInt() ?: 1
        val pageIndex = page - 1

        val groupId = command.strings["group"]
        val group = groupId?.let { ServerGroup.group(it) }

        response.respond {
            createMessage(pageIndex, group, this)
        }
    }

    kord.on<ButtonInteractionCreateEvent> {
        val customId = interaction.componentId
        val match = buttonRegex.find(customId) ?: return@on

        val pageIndex = match.groups[1]?.value?.toIntOrNull() ?: return@on

        val groupId = match.groups[2]?.value ?: return@on
        val group = ServerGroup.group(groupId)

        interaction.updateEphemeralMessage {
            createMessage(pageIndex, group, this)
        }
    }
}

private fun createMessage(pageIndex: Int, group: ServerGroup?, messageBuilder: MessageBuilder) {
    messageBuilder.components = mutableListOf()

    val groupUuid = group?.uuid ?: dummyUuid
    val servers = (group?.toList() ?: Server.servers()).toMutableList().chunked(10)

    if (pageIndex !in servers.indices) {
        messageBuilder.content = ":warning: 間違った場所に来てしまったようです"
        messageBuilder.actionRow {
            interactionButton(ButtonStyle.Primary, "server-list-$groupUuid-0") {
                label = "サーバーリストの先頭へ"
            }
        }
        return
    }

    val stringBuilder = StringBuilder()
    stringBuilder.append(":notepad_spiral: **サーバーリスト (${pageIndex + 1}/${servers.size})**")
    if (group != null) {
        stringBuilder.append(" in ${group.appNotation()}")
    }

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
                interactionButton(ButtonStyle.Secondary, "server-list-$groupUuid-$previousPageIndex") {
                    label = "◀ 前へ"
                }
            }

            if (shouldShowNext) {
                interactionButton(ButtonStyle.Secondary, "server-list-$groupUuid-$nextPageIndex") {
                    label = "次へ ▶"
                }
            }
        }
    }
}