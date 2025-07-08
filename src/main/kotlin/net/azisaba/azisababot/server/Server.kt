package net.azisaba.azisababot.server

import net.azisaba.azisababot.server.endpoint.Endpoint
import net.azisaba.azisababot.server.snapshot.Snapshots
import org.jetbrains.exposed.v1.core.ResultRow
import java.util.UUID

interface Server {
    val uuid: UUID

    val serverId: String

    var displayName: String

    val endpoints: List<Endpoint>

    val snapshots: Snapshots

    fun remove()

    companion object {
        val ID_REGEX: Regex = Regex("^(?!.*server)[a-z0-9_]{1,16}$")
        val NAME_REGEX: Regex = Regex("^.{0,16}$")

        internal val instances: MutableSet<Server> = mutableSetOf()

        fun server(id: String): Server? = instances.find { it.serverId == id }

        fun servers(): Set<Server> = instances.toSet()

        internal fun load(row: ResultRow): Server = ServerImpl(
            uuid = row[ServerTable.uuid],
            serverId = row[ServerTable.serverId],
            displayName = row[ServerTable.displayName]
        )
    }

    @ServerDsl
    interface Builder {
        var serverId: String?

        var displayName: String?

        val endpoints: MutableList<Endpoint>

        fun build(): Server
    }
}