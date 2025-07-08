package net.azisaba.azisababot.server.status

import net.azisaba.azisababot.packet.ClientboundStatusResponsePacket
import net.azisaba.azisababot.server.snapshot.SnapshotTable
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

        fun status(jsonResponse: ClientboundStatusResponsePacket.JsonResponse): ServerStatus = status(
            version = jsonResponse.version.name,
            protocol = jsonResponse.version.protocol,
            onlinePlayers = jsonResponse.players.online,
            maxPlayers = jsonResponse.players.max,
            favicon = jsonResponse.favicon,
            description = jsonResponse.description
        )

        internal fun status(row: ResultRow): ServerStatus? {
            val version = row[SnapshotTable.dummy.version] ?: return null
            val protocol = row[SnapshotTable.dummy.protocol] ?: return null
            val onlinePlayers = row[SnapshotTable.dummy.onlinePlayers] ?: return null
            val maxPlayers = row[SnapshotTable.dummy.maxPlayers] ?: return null
            val favicon = row[SnapshotTable.dummy.favicon] ?: return null
            val description = row[SnapshotTable.dummy.description] ?: return null
            return status(
                version = version,
                protocol = protocol,
                onlinePlayers = onlinePlayers,
                maxPlayers = maxPlayers,
                favicon = favicon,
                description = GsonComponentSerializer.gson().deserialize(description)
            )
        }
    }
}

private data class ServerStatusImpl(
    override val version: String,
    override val protocol: Int,
    override val onlinePlayers: Int,
    override val maxPlayers: Int,
    override val favicon: String,
    override val description: Component
) : ServerStatus