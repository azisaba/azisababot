package net.azisaba.azisababot.server

import net.azisaba.azisababot.crawler.snapshot.Snapshots
import net.azisaba.azisababot.server.endpoints.Endpoints
import org.jetbrains.exposed.v1.core.ResultRow
import java.net.InetSocketAddress
import java.util.*

interface Server {
    val uuid: UUID

    val serverId: String

    var displayName: String

    val endpoints: Endpoints

    val snapshots: Snapshots

    fun remove()

    companion object {
        val SERVER_ID_REGEX: Regex = Regex("^(?!.*server)[a-z0-9_]{1,16}$")
        val DISPLAY_NAME_REGEX: Regex = Regex("^.{0,16}$")

        internal val instances: MutableSet<Server> = mutableSetOf()

        fun server(uuid: UUID): Server? = instances.find { it.uuid == uuid }

        fun server(serverId: String): Server? = instances.find { it.serverId == serverId }

        fun server(block: Builder.() -> Unit): Server = ServerImpl.BuilderImpl().apply(block).build()

        fun servers(): Set<Server> = instances.sortedBy { it.serverId }.toSet()

        internal fun load(row: ResultRow): Server = ServerImpl(
            uuid = row[ServerTable.uuid],
            serverId = row[ServerTable.serverId],
            displayName = row[ServerTable.displayName]
        )
    }

    interface Endpoint {
        val host: String

        val port: Int

        fun toINetSocketAddress(): InetSocketAddress

        companion object {
            fun of(host: String, port: Int): Endpoint = ServerImpl.EndpointImpl(host, port)
        }
    }

    @ServerDsl
    interface Builder {
        var uuid: UUID

        var serverId: String?

        var displayName: String?

        fun build(): Server
    }
}

@DslMarker
annotation class ServerDsl()