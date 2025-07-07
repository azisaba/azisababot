package net.azisaba.azisababot.server.snapshot

import net.azisaba.azisababot.server.snapshot.impl.SnapshotImpl

interface Snapshot {
    val timestamp: Long

    val version: String?

    val protocol: Int?

    val onlinePlayers: Int?

    val maxPlayers: Int?

    val ping: Long?

    companion object {
        fun snapshot(timestamp: Long, version: String?, protocol: Int?, onlinePlayers: Int?, maxPlayers: Int?, ping: Long?): Snapshot =
            SnapshotImpl(timestamp, version, protocol, onlinePlayers, maxPlayers, ping)
    }
}