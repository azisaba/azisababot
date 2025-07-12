package net.azisaba.azisababot.app.command

import dev.kord.common.Locale
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import net.azisaba.azisababot.crawler.schedule.CrawlSchedule
import net.azisaba.azisababot.cronParser
import net.azisaba.azisababot.server.group.ServerGroup
import net.azisaba.azisababot.util.i18n

private const val COMMAND_NAME: String = "abt-create-schedule"

suspend fun abtCreateScheduleCommand(guild: Guild) = guild.createChatInputCommand(COMMAND_NAME, "Create a crawl schedule") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "クロールスケジュールを作成します"
    )

    string("id", "Crawl schedule ID, can contain a-z, 0-9, and underscores") {
        required = true
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to  "クロールスケジュールのID a-z、0-9、アンダーバーが使用できます"
        )
    }

    string("cron", "Cron expression to run the crawl") {
        required = true
        maxLength = 86
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "クロールを実行するcron式"
        )
    }

    string("target", "Server group to crawl (optional)") {
        required = false
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "クロールの対象となるサーバーグループ (任意)"
        )
        for (group in ServerGroup.groups()) {
            choice(group.nameOrId, group.id)
        }
    }
}

fun abtCreateScheduleCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == COMMAND_NAME } ?: return@on

    val id = command.strings["id"]!!

    val cronExpression = command.strings["cron"]!!

    val groupId = command.strings["group"]
    val group = groupId?.let { ServerGroup.group(it) }

    val response = interaction.deferPublicResponse()

    try {
        val cron = cronParser.parse(cronExpression)
        val schedule = CrawlSchedule.schedule {
            this.id = id
            this.cron = cron
            this.group = group
        }
        response.respond {
            content = i18n("command.abm_create_schedule.success", schedule.id)
        }
    } catch (e: Exception) {
        response.respond {
            content = i18n("command.abm_create_schedule.failure", e.message)
        }
    }
}