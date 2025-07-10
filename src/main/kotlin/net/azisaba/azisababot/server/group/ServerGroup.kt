package net.azisaba.azisababot.server.group

import net.azisaba.azisababot.Identified
import net.azisaba.azisababot.server.Server
import org.jetbrains.exposed.v1.core.ResultRow
import java.util.UUID

interface ServerGroup : Identified, Iterable<Server> {
    var groupId: String

    var displayName: String

    val size: Int

    operator fun plusAssign(server: Server)

    operator fun minusAssign(server: Server)

    operator fun contains(server: Server): Boolean

    fun appNotation(): String

    fun remove()

    fun clear()

    companion object {
        val GROUP_ID_REGEX: Regex = Regex("^(?!.*all)[a-z0-9_]{1,16}$")
        val DISPLAY_NAME_REGEX: Regex = Regex("^.{0,16}$")

        internal val instances: MutableSet<ServerGroup> = mutableSetOf(AllServerGroup)

        fun group(uuid: UUID): ServerGroup? = instances.find { it.uuid == uuid }

        fun group(groupId: String): ServerGroup? = instances.find { it.groupId == groupId }

        fun group(block: Builder.() -> Unit): ServerGroup = ServerGroupImpl.BuilderImpl().apply(block).build()

        fun groups(): Set<ServerGroup> = instances.toSet()

        fun groups(server: Server): Set<ServerGroup> = instances.filter { server in it }.toSet()

        internal fun load(row: ResultRow): ServerGroup = ServerGroupImpl(
            uuid = row[ServerGroupTable.uuid],
            groupId = row[ServerGroupTable.groupId],
            displayName = row[ServerGroupTable.displayName]
        )
    }

    @ServerGroupDsl
    interface Builder {
        var uuid: UUID

        var groupId: String?

        var displayName: String?

        fun build(): ServerGroup
    }
}

@DslMarker
annotation class ServerGroupDsl()