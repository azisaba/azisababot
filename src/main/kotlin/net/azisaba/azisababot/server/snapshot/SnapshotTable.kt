package net.azisaba.azisababot.server.snapshot

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table

open class SnapshotTable(tableName: String) : Table(tableName) {
    val timestamp: Column<Long> = long("timestamp")

    val version: Column<String?> = varchar("version", 56).nullable()

    val protocol: Column<Int?> = integer("protocol").nullable()

    val onlinePlayers: Column<Int?> = integer("online_players").nullable()

    val maxPlayers: Column<Int?> = integer("max_players").nullable()

    val favicon: Column<String?> = varchar("favicon", 225).nullable()

    val description: Column<String?> = varchar("description", 225).nullable()

    val ping: Column<ULong?> = ulong("ping").nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(timestamp)

    companion object {
        internal val dummy: SnapshotTable = SnapshotTable("dummy")
    }
}