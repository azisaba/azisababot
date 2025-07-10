package net.azisaba.azisababot.app.command.endpoint

import dev.kord.common.Color
import dev.kord.common.Locale
import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.live.live
import dev.kord.core.live.on
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.embed
import net.azisaba.azisababot.server.Server

suspend fun endpointEditCommand(guild: Guild) = guild.createChatInputCommand("endpoint-edit", "Edit the endpoints of a server") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "サーバーのエンドポイントを編集します"
    )

    string("server", "Server to edit") {
        required = true
        maxLength = 16
    }
}

@OptIn(KordPreview::class)
fun endpointEditCommand(kord: Kord) = kord.on<ChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == "endpoint-edit" } ?: return@on
    val response = interaction.deferPublicResponse()

    val serverId = command.strings["server"]!!
    val server = Server.server(serverId)

    if (server == null) {
        response.respond {
            content = ":x: `${serverId}` は無効なサーバーIDです"
        }
        return@on
    }

    val responseMessage = response.respond {
        embed {
            title = ":pencil: エンドポイントを編集"
            description = """${server.appNotation()} のエンドポイントを編集します
                |**操作方法：**
            """.trimMargin()
            color = Color(88, 101, 242)
            field {
                name = ":arrow_up_small:"
                value = "優先度を上へ"
                inline = true
            }
            field {
                name = ":arrow_down_small:"
                value = "優先度を下へ"
                inline = true
            }
            field {
                name = ":wastebasket:"
                value = "削除"
                inline = true
            }
        }
    }.message

    val channel = interaction.channel
    val endpoints = server.endpoints.toList()

    val messages = mutableListOf<Message>()

    suspend fun success(message: String) {
        responseMessage.edit {
            content = ":pencil: $message"
            embeds = mutableListOf()
        }
        messages.forEach { it.delete() }
    }

    suspend fun failure(message: String) {
        responseMessage.edit {
            content = ":x: $message"
            embeds = mutableListOf()
        }
        messages.forEach { it.delete() }
    }

    suspend fun sendEndpointController(endpoint: Server.Endpoint, priority: Int): Message {
        val message = channel.createMessage(endpoint.toString())

        with(message) {
            if (priority != endpoints.lastIndex) {
                addReaction(ReactionEmoji.Unicode("\uD83D\uDD3C"))
            }

            if (priority != 0) {
                addReaction(ReactionEmoji.Unicode("\uD83D\uDD3D"))
            }

            addReaction(ReactionEmoji.Unicode("\uD83D\uDDD1\uFE0F"))
        }

        message.live().on<ReactionAddEvent> { event ->
            val emoji = event.emoji
            when (emoji.name) {
                "\uD83D\uDD3C" -> {
                    val newPriority = priority + 1
                    try {
                        server.endpoints.move(priority, newPriority)
                        success("$endpoint の優先度を $priority から $newPriority に変更しました")
                    } catch (e: IndexOutOfBoundsException) {
                        failure("$endpoint の優先度を $priority から $newPriority に変更しようとしましたが、失敗しました (${e.message})")
                    }
                }
                "\uD83D\uDD3D" -> {
                    val newPriority = priority - 1
                    try {
                        server.endpoints.move(priority, newPriority)
                        success("$endpoint の優先度を $priority から $newPriority に変更しました")
                    } catch (e: IndexOutOfBoundsException) {
                        failure("$endpoint の優先度を $priority から $newPriority に変更しようとしましたが、失敗しました (${e.message})")
                    }
                }
                "\uD83D\uDDD1\uFE0F" -> {
                    try {
                        server.endpoints.remove(priority)
                        success("$endpoint を削除しました")
                    } catch (e: Exception) {
                        failure("$endpoint を削除しようとしましたが、失敗しました (${e.message})")
                    }
                }
            }
        }

        messages.add(message)
        return message
    }

    for ((offset, endpoint) in endpoints.withIndex()) {
        val priority = endpoints.lastIndex - offset
        sendEndpointController(endpoint, priority)
    }
}