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
import net.azisaba.azisababot.server.group.ServerGroup
import net.azisaba.azisababot.util.i18n

private const val COMMAND_NAME: String = "abm-group-list"

private const val GROUP_PER_PAGE: Int = 10

private val buttonRegex: Regex = Regex("""group-list-(\d+)""")

suspend fun abmGroupListCommand(guild: Guild) = guild.createChatInputCommand(COMMAND_NAME, "List server groups") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーグループをリスト表示します"
    )

    integer("page", "Page index") {
        required = false
        minValue = 0
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "ページのインデックス"
        )
    }
}

fun abmGroupListCommand(kord: Kord) {
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
    val groups = ServerGroup.groups().chunked(GROUP_PER_PAGE)

    if (groups.isEmpty()) {
        builder.content = i18n("command.abm_group_list.empty")
        return
    }

    if (page !in groups.indices) {
        builder.content = i18n("command.abm_group_list.invalid_page")
        return
    }

    val stringBuilder = StringBuilder()

    stringBuilder.append("**")
    stringBuilder.append(i18n("command.abm_group_list.list.title", page + 1, groups.size))
    stringBuilder.append("**")

    for (group in groups[page]) {
        stringBuilder.append('\n')
        stringBuilder.append("- ")
        stringBuilder.append(i18n("command.abm_group_list.list.element", group.toAppName(), group.size))
    }

    stringBuilder.append('\n')
    stringBuilder.append("-# ")
    stringBuilder.append(i18n("command.abm_group_list.list.footer", groups.flatten().size))

    builder.content = stringBuilder.toString()

    val previousPage = page - 1
    val shouldShowPrevious = previousPage in groups.indices

    val nextPage = page + 1
    val shouldShowNext = nextPage in groups.indices

    if (shouldShowPrevious || shouldShowNext) {
        builder.actionRow {
            if (shouldShowPrevious) {
                interactionButton(ButtonStyle.Secondary, "group-list-$previousPage") {
                    label = i18n("command.abm_group_list.list.previous")
                }
            }

            if (shouldShowNext) {
                interactionButton(ButtonStyle.Secondary, "group-list-$nextPage") {
                    label = i18n("command.abm_group_list.list.next")
                }
            }
        }
    }
}