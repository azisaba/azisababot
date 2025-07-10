package net.azisaba.azisababot.server.snapshots

import net.azisaba.azisababot.server.status.ServerStatus
import org.jetbrains.exposed.v1.core.ResultRow

sealed interface ServerSnapshot {
    val timestamp: Long

    val status: ServerStatus?

    val ping: Long?

    companion object {
        fun snapshot(timestamp: Long, status: ServerStatus?, ping: Long?): ServerSnapshot =
            ServerSnapshotImpl(timestamp, status, ping)

        internal fun snapshot(row: ResultRow, table: ServerSnapshotTable): ServerSnapshot = snapshot(
            timestamp = row[table.timestamp],
            status = ServerStatus.status(row, table),
            ping = row[table.ping]
        )
    }
}

private class ServerSnapshotImpl(
    override val timestamp: Long,
    override val status: ServerStatus?,
    override val ping: Long?
) : ServerSnapshot