package net.azisaba.azisababot.server.snapshot.impl

import net.azisaba.azisababot.server.snapshot.Snapshot

internal data class SnapshotImpl(
    override val timestamp: Long,
    override val version: String?,
    override val protocol: Int?,
    override val onlinePlayers: Int?,
    override val maxPlayers: Int?,
    override val ping: Long?
) : Snapshot
