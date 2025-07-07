package net.azisaba.azisababot.server.snapshot.query

import net.azisaba.azisababot.server.snapshot.Snapshot
import net.azisaba.azisababot.server.snapshot.SnapshotTable
import net.azisaba.azisababot.util.UnixTimeRange

interface SnapshotQuery<O> {
    fun query(table: SnapshotTable, option: O): List<Snapshot>

    companion object {
        val RECENT: SnapshotQuery<Int> = RecentSnapshotQuery
        val TIME_RANGE: SnapshotQuery<UnixTimeRange> = TimeRangeSnapshotQuery
    }
}