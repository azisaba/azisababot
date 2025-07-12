package net.azisaba.azisababot.app.command

import dev.kord.common.Locale
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import net.azisaba.azisababot.crawler.schedule.CrawlSchedule
import net.azisaba.azisababot.util.i18n

private const val COMMAND_NAME: String = "abt-delete-schedule"

suspend fun abmDeleteScheduleCommand(guild: Guild) = guild.createChatInputCommand(COMMAND_NAME, "Delete a crawl schedule") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "クロールスケジュールを削除します"
    )

    string("schedule", "Crawl schedule to be deleted") {
        required = true
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "削除するクロールスケジュール"
        )
        for (schedule in CrawlSchedule.schedules()) {
            choice(schedule.id, schedule.id)
        }
    }
}

fun abmDeleteScheduleCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == COMMAND_NAME } ?: return@on

    val scheduleId = command.strings["schedule"]!!
    val schedule = CrawlSchedule.schedule(scheduleId)
    if (schedule == null) {
        interaction.respondEphemeral {
            content = i18n("command.errors.schedule_not_found", scheduleId)
        }
        return@on
    }

    val response = interaction.deferPublicResponse()

    schedule.cancel()

    response.respond {
        content = i18n("command.abm_delete_schedule.success", schedule.id)
    }
}