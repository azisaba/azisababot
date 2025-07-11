package net.azisaba.azisababot.server.group

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table

object ServerGroupTable : Table("server_group") {
    val id: Column<String> = varchar("id", 16)

    val name: Column<String?> = varchar("name", 16).nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}