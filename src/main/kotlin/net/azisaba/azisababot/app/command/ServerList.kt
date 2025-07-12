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
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.server.group.ServerGroup
import net.azisaba.azisababot.util.i18n
import java.util.*

private const val COMMAND_NAME: String = "abt-server-list"

private const val SERVER_PER_PAGE: Int = 10

private val buttonRegex: Regex = Regex("""server-list-(${ServerGroup.ID_REGEX.pattern.removePrefix("^").removeSuffix("$")})-(\d+)""")

private val dummyGroupId: String = UUID.randomUUID().toString().replace('-', '_')

suspend fun abtServerListCommand(guild: Guild) = guild.createChatInputCommand(COMMAND_NAME, "List the servers") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーをリスト表示します"
    )

    integer("page", "Page index") {
        required = false
        minValue = 0
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "ページのインデックス"
        )
    }

    string("group", "Server group") {
        required = false
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "サーバーグループ"
        )
        for (group in ServerGroup.groups()) {
            choice(group.name ?: group.id, group.id)
        }
    }
}

fun abtServerListCommand(kord: Kord) {
    kord.on<ChatInputCommandInteractionCreateEvent> {
        val command = interaction.command.takeIf { it.rootName == COMMAND_NAME } ?: return@on
        val response = interaction.deferEphemeralResponse()

        val page = command.integers["page"]?.toInt() ?: 0

        val groupId = command.strings["group"]
        val group = groupId?.let { ServerGroup.group(it) }

        response.respond {
            buildPage(page, group, this)
        }
    }

    kord.on<ButtonInteractionCreateEvent> {
        val customId = interaction.componentId
        val match = buttonRegex.find(customId) ?: return@on

        val groupId = match.groups[1]?.value ?: return@on
        val group = ServerGroup.group(groupId)

        val page = match.groups[2]?.value?.toIntOrNull() ?: return@on

        interaction.updateEphemeralMessage {
            buildPage(page, group, this)
        }
    }
}

private fun buildPage(page: Int, group: ServerGroup?, builder: MessageBuilder) {
    val servers = (group?.toList() ?: Server.servers()).toMutableList().chunked(SERVER_PER_PAGE)

    if (servers.isEmpty()) {
        builder.content = i18n("command.abm_server_list.empty")
        return
    }

    if (page !in servers.indices) {
        builder.content = i18n("command.abm_server_list.invalid_page")
        return
    }

    val stringBuilder = StringBuilder()

    stringBuilder.append("**")
    stringBuilder.append(i18n("command.abm_server_list.list.title", page + 1, servers.size))
    if (group != null) {
        stringBuilder.append(" in ${group.toAppName()}")
    }
    stringBuilder.append("**")

    for (server in servers[page]) {
        stringBuilder.append('\n')
        stringBuilder.append("- ")
        stringBuilder.append(i18n("command.abm_server_list.list.element", server.toAppName(), server.toList().size))
    }

    stringBuilder.append('\n')
    stringBuilder.append("-# ")
    stringBuilder.append(i18n("command.abm_server_list.list.footer", servers.flatten().size))

    builder.content = stringBuilder.toString()

    val previousPage = page - 1
    val shouldShowPrevious = previousPage in servers.indices

    val nextPage = page + 1
    val shouldShowNext = nextPage in servers.indices

    if (shouldShowPrevious || shouldShowNext) {
        val groupId = group?.id ?: dummyGroupId
        builder.actionRow {
            if (shouldShowPrevious) {
                interactionButton(ButtonStyle.Secondary, "server-list-$groupId-$previousPage") {
                    label = i18n("command.abm_server_list.list.previous")
                }
            }

            if (shouldShowNext) {
                interactionButton(ButtonStyle.Secondary, "server-list-$groupId-$nextPage") {
                    label = i18n("command.abm_server_list.list.next")
                }
            }
        }
    }
}