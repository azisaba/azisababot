package net.azisaba.azisababot.server

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table

class EndpointTable(server: Server) : Table("endpoint_${server.id}") {
    val host: Column<String> = varchar("host", 255)

    val port: Column<Int> = integer("port")

    val priority: Column<Int> = integer("priority").default(0)

    override val primaryKey: PrimaryKey = PrimaryKey(host, port)
}