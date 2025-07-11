package net.azisaba.azisababot.server.group

import net.azisaba.azisababot.Identified
import net.azisaba.azisababot.Nameable
import net.azisaba.azisababot.app.AppFriendly
import net.azisaba.azisababot.server.Server
import org.jetbrains.exposed.v1.core.ResultRow

interface ServerGroup : AppFriendly, Identified, Iterable<Server>, Nameable {
    val nameOrId: String

    val size: Int

    operator fun plusAssign(server: Server)

    operator fun minusAssign(server: Server)

    operator fun contains(server: Server): Boolean

    fun clear()

    fun remove()

    companion object {
        val ID_REGEX: Regex = Regex("^[a-z0-9_]{1,16}$")
        val NAME_REGEX: Regex = Regex("^.{0,16}$")

        internal val instances: MutableSet<ServerGroup> = mutableSetOf()

        fun group(id: String): ServerGroup? = instances.find { it.id == id }

        fun group(block: Builder.() -> Unit): ServerGroup = ServerGroupImpl.BuilderImpl().apply(block).build()

        fun groups(): Set<ServerGroup> = instances.sortedBy { it.id }.toSet()

        fun groups(server: Server): Set<ServerGroup> = groups().filter { server in it }.toSet()

        internal fun load(row: ResultRow): ServerGroup = ServerGroupImpl(
            id = row[ServerGroupTable.id],
            name = row[ServerGroupTable.name]
        )
    }

    @ServerGroupDsl
    interface Builder {
        var id: String?

        var name: String?

        fun build(): ServerGroup
    }
}

@DslMarker
annotation class ServerGroupDsl()