package net.azisaba.azisababot.server.snapshot

import net.azisaba.azisababot.server.snapshot.query.SnapshotQuery

interface SnapshotsHolder {
    val snapshotTable: SnapshotTable

    fun <O> query(query: SnapshotQuery<O>, option: O): List<Snapshot>

    operator fun plusAssign(value: Snapshot)

    operator fun minusAssign(value: Snapshot)
}