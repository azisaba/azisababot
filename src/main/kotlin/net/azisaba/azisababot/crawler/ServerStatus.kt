package net.azisaba.azisababot.crawler

import net.azisaba.azisababot.client.packet.ClientboundStatusResponse
import net.azisaba.azisababot.crawler.snapshot.SnapshotTable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.jetbrains.exposed.v1.core.ResultRow

sealed interface ServerStatus {
    val version: String

    val protocol: Int

    val onlinePlayers: Int

    val maxPlayers: Int

    val favicon: String

    val description: Component

    companion object {
        fun status(version: String, protocol: Int, onlinePlayers: Int, maxPlayers: Int, favicon: String, description: Component): ServerStatus =
            ServerStatusImpl(version, protocol, onlinePlayers, maxPlayers, favicon, description)

        fun status(jsonResponse: ClientboundStatusResponse.JsonResponse): ServerStatus = status(
            jsonResponse.version.name,
            jsonResponse.version.protocol,
            jsonResponse.players.online,
            jsonResponse.players.max,
            jsonResponse.favicon,
            jsonResponse.description
        )

        internal fun status(row: ResultRow, table: SnapshotTable): ServerStatus? {
            val version = row[table.version] ?: return null
            val protocol = row[table.protocol] ?: return null
            val onlinePlayers = row[table.onlinePlayers] ?: return null
            val maxPlayers = row[table.maxPlayers] ?: return null
            val favicon = row[table.favicon] ?: return null
            val description = row[table.description]?.let { GsonComponentSerializer.gson().deserialize(it) } ?: return null
            return status(version, protocol, onlinePlayers, maxPlayers, favicon, description)
        }
    }
}

private class ServerStatusImpl(
    override val version: String,
    override val protocol: Int,
    override val onlinePlayers: Int,
    override val maxPlayers: Int,
    override val favicon: String,
    override val description: Component
) : ServerStatus