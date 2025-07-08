package net.azisaba.azisababot.server.endpoint

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table

class EndpointTable(tableName: String) : Table(tableName) {
    val host: Column<String> = varchar("host", 255)

    val port: Column<Int> = integer("port").default(25565).check { it.between(0, 65535) }

    val priority: Column<Int> = integer("priority").uniqueIndex()

    override val primaryKey: PrimaryKey = PrimaryKey(priority)
}