package net.azisaba.azisababot.server.snapshot.query

import net.azisaba.azisababot.server.snapshot.Snapshot
import net.azisaba.azisababot.server.snapshot.SnapshotTable
import net.azisaba.azisababot.server.snapshot.toSnapshot
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal object RecentSnapshotQuery : SnapshotQuery<Int> {
    override fun query(table: SnapshotTable, option: Int): List<Snapshot> = transaction {
        table.selectAll()
            .orderBy(table.timestamp, SortOrder.DESC)
            .limit(option)
            .map { it.toSnapshot() }
    }
}