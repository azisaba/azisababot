package net.azisaba.azisababot.app.command.schedule

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

suspend fun scheduleDeleteCommand(guild: Guild) = guild.createChatInputCommand("schedule-delete", "Delete a crawl schedule") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "クロールスケジュールを削除します"
    )

    string("schedule", "Crawl schedule to be deleted") {
        required = true
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "削除するクロールスケジュール"
        )
    }
}

fun scheduleDeleteCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == "schedule-delete" } ?: return@on

    val scheduleName = command.strings["schedule"]!!
    val schedule = CrawlSchedule.schedule(scheduleName)

    if (schedule == null) {
        interaction.respondEphemeral {
            content = ":x: $scheduleName は無効なクロールスケジュール名です"
        }
        return@on
    }

    val response = interaction.deferEphemeralResponse()

    schedule.remove()

    response.respond {
        content = ":white_check_mark: クロールスケジュール ${schedule.appNotation()} を削除しました"
    }
}