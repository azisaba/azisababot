package net.azisaba.azisababot.app.command.schedule

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
import net.azisaba.azisababot.crawler.schedule.CrawlSchedule

private val buttonRegex: Regex = Regex("""schedule-list-(\d+)""")

suspend fun scheduleListCommand(guild: Guild) = guild.createChatInputCommand("schedule-list", "List crawl schedules") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "クロールスケジュールをリスト表示します"
    )

    integer("page", "Page number") {
        required = false
        minValue = 1
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "ページ番号"
        )
    }
}

fun scheduleListCommand(kord: Kord) {
    kord.on<ChatInputCommandInteractionCreateEvent> {
        val command = interaction.command.takeIf { it.rootName == "schedule-list" } ?: return@on
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

    val schedules = CrawlSchedule.schedules().chunked(10)
    if (pageIndex !in schedules.indices) {
        messageBuilder.content = ":warning: 間違えた場所に来てしまったようです"
        messageBuilder.actionRow {
            interactionButton(ButtonStyle.Primary, "schedule-list-0") {
                label = "クロールスケジュールリストの先頭へ"
            }
        }
        return
    }

    val stringBuilder = StringBuilder()
    stringBuilder.append(":notepad_spiral: **クロールスケジュールリスト (${pageIndex + 1}/${schedules.size})**")

    for (schedule in schedules[pageIndex]) {
        stringBuilder.append('\n')
        stringBuilder.append("- ${schedule.appNotation()} (`${schedule.group.displayName}`)")
    }

    stringBuilder.append('\n')
    stringBuilder.append("-# 全${schedules.flatten().size}件のクロールスケジュール")

    messageBuilder.content = stringBuilder.toString()

    val previousPageIndex = pageIndex - 1
    val shouldShowPrevious = previousPageIndex in schedules.indices

    val nextPageIndex = pageIndex + 1
    val shouldShowNext = nextPageIndex in schedules.indices

    if (shouldShowPrevious || shouldShowNext) {
        messageBuilder.actionRow {
            if (shouldShowPrevious) {
                interactionButton(ButtonStyle.Secondary, "schedule-list-$previousPageIndex") {
                    label = "◀ 前へ"
                }
            }

            if (shouldShowNext) {
                interactionButton(ButtonStyle.Secondary, "schedule-list-$nextPageIndex") {
                    label = "次へ ▶"
                }
            }
        }
    }
}