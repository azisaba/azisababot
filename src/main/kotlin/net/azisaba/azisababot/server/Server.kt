package net.azisaba.azisababot.server

import net.azisaba.azisababot.server.impl.ServerImpl
import net.azisaba.azisababot.server.snapshot.SnapshotsHolder

interface Server : SnapshotsHolder {
    val id: String

    var name: String

    var host: String

    var port: Int

    fun remove()

    companion object {
        val ID_REGEX: Regex = Regex("^(?!.*server)[a-z0-9_]{1,16}$")
        val NAME_REGEX: Regex = Regex("^.{0,16}$")

        internal val instances: MutableSet<Server> = mutableSetOf()

        fun server(id: String): Server? = instances.find { it.id == id }

        fun servers(): Set<Server> = instances.toSet()

        internal fun load(id: String, name: String, host: String, port: Int): Server = ServerImpl(id, name, host, port)
    }

    @ServerDsl
    interface Builder {
        var id: String?

        var name: String?

        var host: String?

        var port: Int

        fun build(): Server
    }
}

@DslMarker
annotation class ServerDsl()
