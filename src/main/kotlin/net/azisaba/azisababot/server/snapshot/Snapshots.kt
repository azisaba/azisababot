package net.azisaba.azisababot.server.snapshot

import net.azisaba.azisababot.server.snapshot.query.RecentSnapshotQuery
import net.azisaba.azisababot.server.snapshot.query.TimeRangeSnapshotQuery
import net.azisaba.azisababot.util.UnixTimeRange

interface Snapshots {
    operator fun plusAssign(value: Snapshot)

    fun <O> query(query: Query<O>, option: O)

    interface Query<O> {
        fun query(table: SnapshotTable, option: O): List<Snapshot>

        companion object {
            val RECENT: Query<Int> = RecentSnapshotQuery
            val TIME_RANGE: Query<UnixTimeRange> = TimeRangeSnapshotQuery
        }
    }
}