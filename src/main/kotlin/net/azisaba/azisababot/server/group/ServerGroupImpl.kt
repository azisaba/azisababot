package net.azisaba.azisababot.server.group

import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.server.ServerTable
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.*

internal class ServerGroupImpl(
    override val uuid: UUID,
    groupId: String,
    displayName: String
) : ServerGroup {
    override var groupId: String = groupId
        set(value) {
            require(ServerGroup.GROUP_ID_REGEX.matches(value)) { "Invalid group ID: must match the pattern ${ServerGroup.GROUP_ID_REGEX.pattern}" }
            field = value
            transaction {
                if (ServerGroupTable.selectAll().where { ServerGroupTable.groupId eq value }.any()) {
                    throw IllegalStateException("This group ID is already in use")
                }

                memberTable.update({ ServerGroupTable.uuid eq uuid }) {
                    it[ServerGroupTable.groupId] = value
                }
            }
        }

    override var displayName: String = displayName
        set(value) {
            require(ServerGroup.DISPLAY_NAME_REGEX.matches(value)) { "Invalid display name: must match the pattern ${ServerGroup.DISPLAY_NAME_REGEX}" }
            field = value
            transaction {
                ServerTable.update({ ServerGroupTable.uuid eq uuid }) {
                    it[displayName] = value
                }
            }
        }

    override val size: Int
        get() = members.size

    private val members: MutableSet<Server> = mutableSetOf()

    private val memberTable: ServerGroupMemberTable = ServerGroupMemberTable(this).also {
        transaction {
            SchemaUtils.create(it)
        }
    }

    init {
        ServerGroup.instances += this
        transaction {
            memberTable.selectAll().forEach { row ->
                val uuid = row[memberTable.uuid]
                members += Server.server(uuid)!!
            }
        }
    }

    override fun plusAssign(server: Server) {
        require(server !in members) { "This server is already a member of the group" }
        members += server
        transaction {
            memberTable.insert {
                it[uuid] = server.uuid
            }
        }
    }

    override fun minusAssign(server: Server) {
        require(server in members) { "This server is not a member of the group" }
        members -= server
        transaction {
            memberTable.deleteWhere { uuid eq server.uuid }
        }
    }

    override fun contains(server: Server): Boolean = server in members

    override fun iterator(): Iterator<Server> = members.iterator()

    override fun appNotation(): String = "$displayName (`$groupId`)"

    override fun clear() {
        ServerGroup.instances -= this
        transaction {
            ServerGroupTable.deleteWhere { uuid eq this@ServerGroupImpl.uuid }
            SchemaUtils.drop(memberTable)
        }
    }

    override fun remove() {
        ServerGroup.instances -= this
        transaction {
            ServerGroupTable.deleteWhere { uuid eq this@ServerGroupImpl.uuid }
            SchemaUtils.drop(memberTable)
        }
    }

    internal class BuilderImpl : ServerGroup.Builder {
        override var uuid: UUID = UUID.randomUUID()

        override var groupId: String? = null

        override var displayName: String? = null

        override fun build(): ServerGroup {
            checkNotNull(groupId) { "Group ID not set" }
            checkNotNull(displayName) { "Display name not set" }

            check(ServerGroup.GROUP_ID_REGEX.matches(groupId!!)) {
                "Invalid group ID: must match the pattern ${ServerGroup.GROUP_ID_REGEX.pattern}"
            }

            check(ServerGroup.DISPLAY_NAME_REGEX.matches(displayName!!)) {
                "Invalid display name: must match the pattern ${ServerGroup.DISPLAY_NAME_REGEX.pattern}"
            }

            transaction {
                check(ServerGroupTable.selectAll().where { ServerGroupTable.uuid eq uuid }.none()) {
                    "UUID is already in use: $uuid"
                }

                check(ServerGroupTable.selectAll().where { ServerGroupTable.groupId eq groupId!! }.none()) {
                    "Group ID is already in use: $groupId"
                }

                ServerGroupTable.insert {
                    it[uuid] = this@BuilderImpl.uuid
                    it[groupId] = this@BuilderImpl.groupId!!
                    it[displayName] = this@BuilderImpl.displayName!!
                }
            }

            return ServerGroupImpl(uuid, groupId!!, displayName!!)
        }
    }
}