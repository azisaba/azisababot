package net.azisaba.azisababot.app.command

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
import net.azisaba.azisababot.util.i18n

private const val COMMAND_NAME: String = "abm-schedule-list"

private const val SCHEDULE_PER_PAGE: Int = 10

private val buttonRegex: Regex = Regex("""schedule-list-(\d+)""")

suspend fun abmScheduleListCommand(guild: Guild) = guild.createChatInputCommand(COMMAND_NAME, "List crawl schedules") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "クロールスケジュールをリスト表示します"
    )

    integer("page", "Page index") {
        required = false
        minValue = 0
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "ページのインデックス"
        )
    }
}

fun abmScheduleListCommand(kord: Kord) {
    kord.on<ChatInputCommandInteractionCreateEvent> {
        val command = interaction.command.takeIf { it.rootName == COMMAND_NAME } ?: return@on
        val response = interaction.deferEphemeralResponse()

        val page = command.integers["page"]?.toInt() ?: 0

        response.respond {
            buildPage(page, this)
        }
    }

    kord.on<ButtonInteractionCreateEvent> {
        val customId = interaction.componentId
        val match = buttonRegex.find(customId) ?: return@on

        val page = match.groups[1]?.value?.toIntOrNull() ?: return@on

        interaction.updateEphemeralMessage {
            buildPage(page, this)
        }
    }
}

private fun buildPage(page: Int, builder: MessageBuilder) {
    val schedules = CrawlSchedule.schedules().chunked(SCHEDULE_PER_PAGE)

    if (schedules.isEmpty()) {
        builder.content = i18n("command.abm_schedule_list.empty")
        return
    }

    if (page !in schedules.indices) {
        builder.content = i18n("command.abm_schedule_list.invalid_page")
        return
    }

    val stringBuilder = StringBuilder()

    stringBuilder.append("**")
    stringBuilder.append(i18n("command.abm_schedule_list.list.title", page + 1, schedules.size))
    stringBuilder.append("**")

    for (schedule in schedules[page]) {
        stringBuilder.append('\n')
        stringBuilder.append("- ")
        stringBuilder.append(i18n("command.abm_schedule_list.list.element", schedule.id, schedule.cron.asString(), schedule.target?.toAppName()))
    }

    stringBuilder.append('\n')
    stringBuilder.append("-# ")
    stringBuilder.append(i18n("command.abm_schedule_list.list.footer", schedules.flatten().size))

    builder.content = stringBuilder.toString()

    val previousPage = page - 1
    val shouldShowPrevious = previousPage in schedules.indices

    val nextPage = page + 1
    val shouldShowNext = nextPage in schedules.indices

    if (shouldShowPrevious || shouldShowNext) {
        builder.actionRow {
            if (shouldShowPrevious) {
                interactionButton(ButtonStyle.Secondary, "schedule-list-$previousPage") {
                    label = i18n("command.abm_schedule_list.list.previous")
                }
            }

            if (shouldShowNext) {
                interactionButton(ButtonStyle.Secondary, "schedule-list-$nextPage") {
                    label = i18n("command.abm_schedule_list.list.next")
                }
            }
        }
    }
}