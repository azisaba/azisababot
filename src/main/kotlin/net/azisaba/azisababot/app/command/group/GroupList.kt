package net.azisaba.azisababot.app.command.group

import dev.kord.common.Locale
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.behavior.interaction.updateEphemeralMessage
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import net.azisaba.azisababot.server.group.ServerGroup

private val buttonRegex: Regex = Regex("""group-list-(\d+)""")

suspend fun groupListCommand(guild: Guild) = guild.createChatInputCommand("group-list", "List server groups") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーグループをリスト表示します"
    )

    integer("page", "Page number") {
        required = false
        minValue = 1
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "ページ番号"
        )
    }
}

fun groupListCommand(kord: Kord) {
    kord.on<ChatInputCommandInteractionCreateEvent> {
        val command = interaction.command.takeIf { it.rootName == "group-list" } ?: return@on
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

        interaction.updateEphemeralMessage {
            createMessage(pageIndex, this)
        }
    }
}

private fun createMessage(pageIndex: Int, messageBuilder: MessageBuilder) {
    messageBuilder.components = mutableListOf()

    val groups = ServerGroup.groups().chunked(10)
    if (pageIndex !in groups.indices) {
        messageBuilder.content = ":warning: 間違えた場所に来てしまったようです"
        messageBuilder.actionRow {
            interactionButton(ButtonStyle.Primary, "group-list-0") {
                label = "サーバーグループリストの先頭へ"
            }
        }
        return
    }

    val stringBuilder = StringBuilder()
    stringBuilder.append(":notepad_spiral: **サーバーグループリスト (${pageIndex + 1}/${groups.size})**")

    for (group in groups[pageIndex]) {
        stringBuilder.append('\n')
        stringBuilder.append("- ${group.appNotation()} (${group.size}個のサーバー)")
    }

    stringBuilder.append('\n')
    stringBuilder.append("-# 全${groups.flatten().size}件のサーバーグループ")

    messageBuilder.content = stringBuilder.toString()

    val previousPageIndex = pageIndex - 1
    val shouldShowPrevious = previousPageIndex in groups.indices

    val nextPageIndex = pageIndex + 1
    val shouldShowNext = nextPageIndex in groups.indices

    if (shouldShowPrevious || shouldShowNext) {
        messageBuilder.actionRow {
            if (shouldShowPrevious) {
                interactionButton(ButtonStyle.Secondary, "group-list-$previousPageIndex") {
                    label = "◀ 前へ"
                }
            }

            if (shouldShowNext) {
                interactionButton(ButtonStyle.Secondary, "group-list-$nextPageIndex") {
                    label = "次へ ▶"
                }
            }
        }
    }
}