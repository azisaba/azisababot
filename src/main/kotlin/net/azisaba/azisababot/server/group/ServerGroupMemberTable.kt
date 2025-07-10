package net.azisaba.azisababot.server.group

import net.azisaba.azisababot.server.ServerTable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import java.util.UUID

class ServerGroupMemberTable(group: ServerGroup) : Table("group_${group.uuid}") {
    val uuid: Column<UUID> = uuid("uuid").references(ServerTable.uuid)

    override val primaryKey: PrimaryKey = PrimaryKey(uuid)
}