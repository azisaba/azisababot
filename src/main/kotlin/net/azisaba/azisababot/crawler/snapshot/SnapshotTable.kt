package net.azisaba.azisababot.crawler.snapshot

import net.azisaba.azisababot.server.Server
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table

class SnapshotTable(server: Server) : Table("snapshot_${server.uuid.toString().replace('-', '_')}") {
    val timestamp: Column<Long> = long("timestamp").index()

    val version: Column<String?> = varchar("version", 32).nullable()

    val protocol: Column<Int?> = integer("protocol").nullable()

    val onlinePlayers: Column<Int?> = integer("online_players").nullable()

    val maxPlayers: Column<Int?> = integer("max_players").nullable()

    val favicon: Column<String?> = varchar("favicon", 225).nullable()

    val description: Column<String?> = varchar("description", 1024).nullable()

    val ping: Column<Long?> = long("ping").nullable()
}