package net.azisaba.azisababot.server.group

import net.azisaba.azisababot.server.Server
import java.util.*

internal object AllServerGroup : ServerGroup {
    override val uuid: UUID = UUID.randomUUID()

    override var groupId: String = "all"

    override var displayName: String = groupId

    override val size: Int = Server.servers().size

    override fun plusAssign(server: Server) {
    }

    override fun minusAssign(server: Server) {
    }

    override fun contains(server: Server): Boolean = true

    override fun appNotation(): String = groupId

    override fun remove() {
    }

    override fun clear() {
    }

    override fun iterator(): Iterator<Server> = Server.servers().iterator()
}