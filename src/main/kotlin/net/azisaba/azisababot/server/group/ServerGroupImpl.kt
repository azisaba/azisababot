package net.azisaba.azisababot.server.group

import net.azisaba.azisababot.app.updateServerGroupCommands
import net.azisaba.azisababot.server.Server
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal class ServerGroupImpl(
    override val id: String,
    name: String?
) : ServerGroup {
    override var name: String? = name
        set(value) {
            require(value == null || ServerGroup.NAME_REGEX.matches(value)) {
                "Invalid name: must match the pattern ${ServerGroup.NAME_REGEX.pattern}"
            }
            field = value
            transaction {
                table.update({ ServerGroupTable.id eq id }) {
                    it[ServerGroupTable.name] = value
                }
            }
        }

    override val nameOrId: String
        get() = name ?: id

    override val size: Int
        get() = servers.size

    private val servers: MutableSet<Server> = mutableSetOf()

    private val table: ServerGroupServerTable = ServerGroupServerTable(this).also {
        transaction {
            SchemaUtils.create(it)
        }
    }

    init {
        ServerGroup.instances += this
        transaction {
            table.selectAll().forEach { row ->
                val serverId = row[table.server]
                servers += Server.server(serverId)!!
            }
        }
    }

    override fun plusAssign(server: Server) {
        require(server !in servers) { "The server is already in the server group" }
        servers += server
        transaction {
            table.insert {
                it[this.server] = server.id
            }
        }
    }

    override fun minusAssign(server: Server) {
        require(server in servers) { "The server is not already part of the server group" }
        servers -= server
        transaction {
            table.deleteWhere { table.server eq server.id }
        }
    }

    override fun contains(server: Server): Boolean = server in servers

    override fun iterator(): Iterator<Server> = servers.iterator()

    override fun clear() {
        servers.clear()
        transaction {
            table.deleteAll()
        }
    }

    override fun remove() {
        ServerGroup.instances -= this

        updateServerGroupCommands()

        transaction {
            ServerGroupTable.deleteWhere { id eq this@ServerGroupImpl.id }
            SchemaUtils.drop(table)
        }
    }

    override fun toAppName(): String = if (name != null) "$name (`$id`)" else id

    internal class BuilderImpl : ServerGroup.Builder {
        override var id: String? = null

        override var name: String? = null

        override fun build(): ServerGroup {
            checkNotNull(id) { "ID not set" }

            check(ServerGroup.ID_REGEX.matches(id!!)) {
                "Invalid ID: must match the pattern ${ServerGroup.ID_REGEX.pattern}"
            }

            check(name == null || ServerGroup.NAME_REGEX.matches(name!!)) {
                "Invalid name: must match the pattern ${ServerGroup.NAME_REGEX.pattern}"
            }

            transaction {
                check(ServerGroupTable.selectAll().where { ServerGroupTable.id eq this@BuilderImpl.id!! }.none()) {
                    "ID is already in use: ${this@BuilderImpl.id}"
                }

                ServerGroupTable.insert {
                    it[id] = this@BuilderImpl.id!!
                    it[name] = this@BuilderImpl.name
                }
            }

            return ServerGroupImpl(id!!, name).also {
                updateServerGroupCommands()
            }
        }
    }
}