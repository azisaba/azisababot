package net.azisaba.azisababot.server.snapshot.query

import net.azisaba.azisababot.server.snapshot.Snapshot
import net.azisaba.azisababot.server.snapshot.SnapshotTable
import net.azisaba.azisababot.server.snapshot.Snapshots
import net.azisaba.azisababot.util.UnixTimeRange
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal object TimeRangeSnapshotQuery : Snapshots.Query<UnixTimeRange> {
    override fun query(table: SnapshotTable, option: UnixTimeRange): List<Snapshot> = transaction {
        table.selectAll()
            .where { table.timestamp.between(option.start, option.end) }
            .map { Snapshot.snapshot(it) }
    }
}