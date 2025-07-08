package net.azisaba.azisababot.server.snapshot

import net.azisaba.azisababot.server.status.ServerStatus
import org.jetbrains.exposed.v1.core.ResultRow

sealed interface Snapshot {
    val timestamp: Long

    val status: ServerStatus?

    val ping: Long?

    companion object {
        fun snapshot(timestamp: Long, status: ServerStatus?, ping: Long?): Snapshot = SnapshotImpl(timestamp, status, ping)

        internal fun snapshot(row: ResultRow) = snapshot(
            timestamp = row[SnapshotTable.dummy.timestamp],
            status = ServerStatus.status(row),
            ping = row[SnapshotTable.dummy.ping]?.toLong()
        )
    }
}

private data class SnapshotImpl(
    override val timestamp: Long,
    override val status: ServerStatus?,
    override val ping: Long?
) : Snapshot