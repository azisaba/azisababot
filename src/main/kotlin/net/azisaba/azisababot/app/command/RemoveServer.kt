package net.azisaba.azisababot.app.command

import dev.kord.common.Locale
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.actionRow
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.util.i18n

private const val COMMAND_NAME: String = "abt-remove-server"

private val buttonRegex: Regex = Regex("""remove-server-(${Server.ID_REGEX.pattern.removePrefix("^").removeSuffix("$")})""")

suspend fun abtRemoveServerCommand(guild: Guild) = guild.createChatInputCommand(COMMAND_NAME, "Remove a server from to crawling") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーをクロールの対象から削除します"
    )

    string("server", "Server to be removed") {
        required = true
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "削除するサーバー"
        )
        for (server in Server.servers()) {
            choice(server.name ?: server.id, server.id)
        }
    }
}

fun abtRemoveServerCommand(kord: Kord) {
    kord.on<ChatInputCommandInteractionCreateEvent> {
        val command = interaction.command.takeIf { it.rootName == COMMAND_NAME } ?: return@on

        val serverId = command.strings["server"]!!
        val server = Server.server(serverId)

        if (server == null) {
            interaction.respondEphemeral {
                content = i18n("command.errors.server_not_found", serverId)
            }
            return@on
        }

        interaction.respondPublic {
            content = i18n("command.abm_remove_server.warning")
            actionRow {
                interactionButton(ButtonStyle.Danger, "remove-server-$serverId") {
                    label = i18n("command.abm_remove_server.warning.confirm")
                }
            }
        }
    }

    kord.on<ButtonInteractionCreateEvent> {
        val customId = interaction.componentId
        val match = buttonRegex.find(customId) ?: return@on

        val serverId = match.groupValues[1]
        val server = Server.server(serverId)
        val response = interaction.deferPublicMessageUpdate()
        try {
            server?.remove()
            response.edit {
                content = i18n("command.abm_remove_server.success", server?.toAppName())
                components = mutableListOf()
            }
        } catch (e: Exception) {
            response.edit {
                content = i18n("command.abm_remove_server.failure", e.message)
                components = mutableListOf()
            }
        }
    }
}