package net.azisaba.azisababot.server.group

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import java.util.UUID

object ServerGroupTable : Table("group") {
    val uuid: Column<UUID> = uuid("group")

    val groupId: Column<String> = varchar("group_id", 16)

    val displayName: Column<String> = varchar("display_name", 16)
}