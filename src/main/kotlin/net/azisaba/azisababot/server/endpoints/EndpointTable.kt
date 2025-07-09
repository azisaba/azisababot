package net.azisaba.azisababot.server.endpoints

import net.azisaba.azisababot.server.Server
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table

class EndpointTable(private val server: Server) : Table("endpoint_${server.uuid.toString().replace('-', '_')}") {
    val host: Column<String> = varchar("host", 255)

    val port: Column<Int> = integer("port")

    val priority: Column<Int> = integer("priority")

    override val primaryKey: PrimaryKey = PrimaryKey(priority)
}