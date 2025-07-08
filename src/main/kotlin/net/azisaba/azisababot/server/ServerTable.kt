package net.azisaba.azisababot.server

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import java.util.UUID

object ServerTable : Table("servers") {
    val uuid: Column<UUID> = uuid("uuid")

    val serverId: Column<String> = varchar("server_id", 16).uniqueIndex()

    val displayName: Column<String> = varchar("display_name", 16)

    override val primaryKey: PrimaryKey = PrimaryKey(uuid)
}