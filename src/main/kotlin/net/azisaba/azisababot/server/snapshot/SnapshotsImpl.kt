package net.azisaba.azisababot.server.snapshot

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert

internal class SnapshotsImpl(private val table: SnapshotTable) : Snapshots {
    override fun plusAssign(value: Snapshot) {
        transaction {
            table.upsert {
                it[table.timestamp] = value.timestamp
                it[table.version] = value.status?.version
                it[table.protocol] = value.status?.protocol
                it[table.onlinePlayers] = value.status?.onlinePlayers
                it[table.maxPlayers] = value.status?.maxPlayers
                it[table.favicon] = value.status?.favicon
                it[table.description] = value.status?.description?.let { GsonComponentSerializer.gson().serialize(it) }
            }
        }
    }

    override fun <O> query(query: Snapshots.Query<O>, option: O) {
        query.query(table, option)
    }
}