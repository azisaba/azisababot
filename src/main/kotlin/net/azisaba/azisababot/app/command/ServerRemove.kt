package net.azisaba.azisababot.app.command

import dev.kord.common.Locale
import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Guild
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.live.live
import dev.kord.core.live.on
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import net.azisaba.azisababot.server.Server

suspend fun serverRemoveCommand(guild: Guild) = guild.createChatInputCommand("server-remove", "Remove the Minecraft server from crawling") {
    descriptionLocalizations = mutableMapOf(
        Locale.JAPANESE to "Minecraft サーバーをクロールの対象から削除します"
    )

    string("server", "Server to be deleted") {
        required = true
        descriptionLocalizations = mutableMapOf(
            Locale.JAPANESE to "削除するサーバーの識別子"
        )
    }
}

@OptIn(KordPreview::class)
fun serverRemoveCommand(kord: Kord) = kord.on<GuildChatInputCommandInteractionCreateEvent> {
    val command = interaction.command.takeIf { it.rootName == "server-remove" } ?: return@on
    val response = interaction.deferPublicResponse()

    val serverId = command.strings["server"]!!
    val server = Server.server(serverId)

    if (server == null) {
        response.respond {
            content = ":x: ${serverId}は無効なサーバー識別子です"
        }
        return@on
    }

    val message = response.respond {
        content = """:warning: **この操作は取り消せません**
            |このサーバーはクロール対象から除外され、作成されたスナップショットはすべて削除されます
            |これらを理解して実行する場合にはリアクションを押してください
        """.trimMargin()
    }.message
    message.addReaction(ReactionEmoji.Unicode("⭕"))
    message.live().on<ReactionAddEvent> { event ->
        if (event.emoji.name == "⭕" && event.getUser() == interaction.user) {
            server.remove()
            message.deleteAllReactions()
            message.edit {
                content = ":white_check_mark: サーバーを削除しました (`${server.serverId}`)"
            }
        } else {
            message.deleteAllReactions()
        }
    }
}