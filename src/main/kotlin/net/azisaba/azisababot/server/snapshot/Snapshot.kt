package net.azisaba.azisababot.server.snapshot

import net.azisaba.azisababot.crawler.ServerStatus
import org.jetbrains.exposed.v1.core.ResultRow

sealed interface Snapshot {
    val timestamp: Long

    val status: ServerStatus?

    val ping: Long?

    companion object {
        fun snapshot(timestamp: Long, status: ServerStatus?, ping: Long?): Snapshot =
            SnapshotImpl(timestamp, status, ping)

        internal fun snapshot(row: ResultRow, table: SnapshotTable): Snapshot = snapshot(
            timestamp = row[table.timestamp],
            status = ServerStatus.status(row, table),
            ping = row[table.ping]
        )
    }
}

private class SnapshotImpl(
    override val timestamp: Long,
    override val status: ServerStatus?,
    override val ping: Long?
) : Snapshot