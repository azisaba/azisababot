package net.azisaba.azisababot.app.command

import com.cronutils.model.Cron
import dev.kord.common.Locale
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import io.ktor.client.request.forms.*
import io.ktor.utils.io.*
import net.azisaba.azisababot.crawler.snapshot.Snapshot
import net.azisaba.azisababot.crawler.snapshot.Snapshots
import net.azisaba.azisababot.cronParser
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.server.group.ServerGroup
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.toBufferedImage
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

private const val COMMAND_NAME: String = "abt-line-graph"

private val GROUP_REGEX: Regex = Regex("""#(${ServerGroup.ID_REGEX.pattern})""")

suspend fun abtLineGraphCommand(guild: Guild) = guild.createChatInputCommand(COMMAND_NAME, "Generate a line graph") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "線グラフを生成します"
    )

    string("with", "Data to be graphed") {
        required = true
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "グラフにするデータ"
        )
        choice("online-player", "online-player")
        choice("ping", "ping")
    }

    string("when", "Time to include in the graph (Cron expression)") {
        required = true
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "グラフに含める時間 (Cron式)"
        )
    }

    string("in", "The server or server group to include in the graph") {
        required = false
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "グラフに含めるサーバーまたはサーバーグループ"
        )
        for (server in Server.servers()) {
            choice(server.nameOrId, server.id)
        }
        for (group in ServerGroup.groups()) {
            choice("#${group.nameOrId}", "#${group.id}")
        }
    }

    string("time-format", "Time format") {
        required = false
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "時刻のフォーマット"
        )
    }

    string("as", "Output file name") {
        required = false
        maxLength = 16
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "出力ファイルの名前"
        )
    }
}

fun abtLineGraphCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == COMMAND_NAME } ?: return@on
    val response = interaction.deferEphemeralResponse()

    val withOpt = command.strings["with"]!!
    val inOpt = command.strings["in"]
    val whenOpt = command.strings["when"]!!
    val timeFormatOpt = command.strings["time-format"] ?: "yy/MM/dd HH:mm"
    val asOpt = command.strings["as"] ?: "chart.png"

    val servers = solveInOpt(inOpt)
    val cron = cronParser.parse(whenOpt)
    val dateTimeFormatter = solveTimeFormatOpt(timeFormatOpt)
    val dataGetter = solveWithOpt(withOpt)

    val dataFrame = buildDataFrame(servers, cron, dateTimeFormatter, dataGetter)

    val plot = dataFrame.plot {
        for (server in servers) {
            line {
                x("time")
                y("value")
                color("server")
            }
        }
    }

    val bufferedImage = plot.toBufferedImage()
    val output = ByteArrayOutputStream().also {
        ImageIO.write(bufferedImage, "png", it)
    }
    response.respond {
        addFile(asOpt, ChannelProvider(null, { ByteReadChannel(output.toByteArray()) }))
    }
}

private fun solveWithOpt(withOpt: String): (Snapshot) -> Any = when (withOpt) {
    "online-player" -> { snapshot ->
        snapshot.status?.onlinePlayers?.toLong() ?: 0
    }
    "ping" -> { snapshot ->
        snapshot.ping ?: 0
    }
    else -> throw IllegalArgumentException()
}

private fun solveInOpt(inOpt: String?): Set<Server> = inOpt?.let { serverOrGroup ->
    val match = GROUP_REGEX.find(serverOrGroup)
    if (match != null) {
        return@let ServerGroup.group(match.groupValues[1])?.toSet()
    } else {
        return@let Server.server(serverOrGroup)?.let { setOf(it) }
    }
} ?: Server.servers()

private fun solveTimeFormatOpt(timeFormatOpt: String): DateTimeFormatter = DateTimeFormatter.ofPattern(timeFormatOpt)
    .withZone(ZoneId.systemDefault())

private fun buildDataFrame(servers: Set<Server>, cron: Cron, dateTimeFormatter: DateTimeFormatter, dataGetter: (Snapshot) -> Any): DataFrame<*> {
    val snapshotsMap = servers.associateWith { Snapshots.snapshots(it) }
    val snapshotListMap = snapshotsMap.entries.associate { it.key to it.value.query().cron(cron).execute().toList() }

    val timestamps = snapshotListMap.values
        .map { it.map { it.timestamp }.toSet() }
        .reduce { acc, set -> acc.intersect(set) }
        .sorted()

    val timeColumn = mutableListOf<Any>()
    val valueColumn = mutableListOf<Any>()
    val serverColumn = mutableListOf<Any>()

    for ((server, snapshots) in snapshotListMap) {
        val snapshotMap = snapshots.associateBy { it.timestamp }
        for (timestamp in timestamps) {
            val instant = Instant.ofEpochMilli(timestamp)
            timeColumn += dateTimeFormatter.format(instant)
            valueColumn += dataGetter(snapshotMap[timestamp]!!)
            serverColumn += server.nameOrId
        }
    }

    return dataFrameOf(
        "time" to timeColumn,
        "value" to valueColumn,
        "server" to serverColumn
    )
}