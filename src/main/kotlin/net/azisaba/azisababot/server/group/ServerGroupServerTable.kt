package net.azisaba.azisababot.server.group

import net.azisaba.azisababot.server.ServerTable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table

class ServerGroupServerTable(group: ServerGroup) : Table("server_group_${group.id}") {
    val server: Column<String> = varchar("server", 16).references(ServerTable.id)

    override val primaryKey: PrimaryKey = PrimaryKey(server)
}