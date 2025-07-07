package net.azisaba.azisababot.server.snapshot

import net.azisaba.azisababot.server.snapshot.impl.SnapshotImpl
import org.jetbrains.exposed.v1.core.ResultRow

internal fun ResultRow.toSnapshot(): Snapshot = SnapshotImpl(
    get(SnapshotTable.Dummy.timestamp),
    get(SnapshotTable.Dummy.version),
    get(SnapshotTable.Dummy.protocol),
    get(SnapshotTable.Dummy.onlinePlayers)?.toInt(),
    get(SnapshotTable.Dummy.maxPlayers)?.toInt(),
    get(SnapshotTable.Dummy.ping)?.toLong()
)