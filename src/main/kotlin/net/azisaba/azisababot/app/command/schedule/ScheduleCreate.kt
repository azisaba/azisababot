package net.azisaba.azisababot.app.command.schedule

import dev.kord.common.Locale
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import net.azisaba.azisababot.crawler.schedule.CrawlSchedule
import net.azisaba.azisababot.cronParser
import net.azisaba.azisababot.server.group.ServerGroup

suspend fun scheduleCreateCommand(guild: Guild) = guild.createChatInputCommand("schedule-create", "Create a crawl schedule") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "クロールスケジュールを作成します"
    )

    string("name", "A unique name for the crawl schedule, can contain a-z, 0-9, and underscores") {
        required = true
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "クロールスケジュールの一意の名前 a-z、0-9、アンダーバーが使用できます"
        )
    }

    string("cron", "Cron expression to run the crawl, e.g. */10 * * * *") {
        required = true
        maxLength = 86
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "クロールを実行するcron式 例：*/10 * * * *"
        )
    }

    string("group", "Crawl target server group") {
        required = false
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "クロールを行うサーバーグループ"
        )
    }
}

fun scheduleCreateCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == "schedule-create" } ?: return@on

    val name = command.strings["name"]!!

    val cronExpression = command.strings["cron"]!!

    val groupId = command.strings["group"]
    val group = groupId?.let { ServerGroup.group(it) }

    try {
        val schedule = CrawlSchedule.schedule {
            this.name = name
            this.cron = cronParser.parse(cronExpression)
            group?.let {
                this.group = group
            }
        }
        interaction.respondPublic {
            content = ":white_check_mark: クロールスケジュールを作成しました ${schedule.appNotation()}"
        }
    } catch (e: Exception) {
        interaction.respondEphemeral {
            content = ":x: クロールスケジュールの作成に失敗しました (${e.message})"
        }
    }
}