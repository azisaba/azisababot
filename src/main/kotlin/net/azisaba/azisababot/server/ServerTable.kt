package net.azisaba.azisababot.server

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table

object ServerTable : Table("server") {
    val id: Column<String> = varchar("id", 16)

    val name: Column<String> = varchar("name", 16)

    val host: Column<String> = varchar("host", 32)

    val port: Column<Int> = integer("port")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}