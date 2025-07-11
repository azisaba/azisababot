package net.azisaba.azisababot.server

import net.azisaba.azisababot.Identified
import net.azisaba.azisababot.Nameable
import net.azisaba.azisababot.app.AppFriendly
import org.jetbrains.exposed.v1.core.ResultRow
import java.net.InetSocketAddress

interface Server : AppFriendly, Identified, Iterable<Pair<Int, Server.Endpoint>>, Nameable {
    val nameOrId: String

    operator fun get(key: Endpoint): Int?

    operator fun set(key: Endpoint, value: Int?)

    operator fun contains(endpoint: Endpoint): Boolean

    fun clear()

    fun remove()

    companion object {
        val ID_REGEX: Regex = Regex("^[a-z0-9_]{1,16}$")
        val NAME_REGEX: Regex = Regex("^.{0,16}$")

        internal val instances: MutableSet<Server> = mutableSetOf()

        fun server(id: String): Server? = instances.find { it.id == id }

        fun server(block: Builder.() -> Unit): Server = ServerImpl.BuilderImpl().apply(block).build()

        fun servers(): Set<Server> = instances.sortedBy { it.id }.toSet()

        internal fun load(row: ResultRow): Server = ServerImpl(
            id = row[ServerTable.id],
            name = row[ServerTable.name]
        )
    }

    interface Endpoint {
        val host: String

        val port: Int

        fun toINetSocketAddress(): InetSocketAddress

        companion object {
            fun of(host: String, port: Int = 25565): Endpoint = ServerImpl.EndpointImpl(host, port)
        }
    }

    @ServerDsl
    interface Builder {
        var id: String?

        var name: String?

        fun build(): Server
    }
}

@DslMarker
annotation class ServerDsl()