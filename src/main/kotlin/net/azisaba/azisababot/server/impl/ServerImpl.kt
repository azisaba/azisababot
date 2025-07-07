package net.azisaba.azisababot.server.impl

import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.server.ServerTable
import net.azisaba.azisababot.server.snapshot.Snapshot
import net.azisaba.azisababot.server.snapshot.SnapshotTable
import net.azisaba.azisababot.server.snapshot.query.SnapshotQuery
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal class ServerImpl(
    override var id: String,
    name: String,
    host: String,
    port: Int
) : Server {
    override var name: String = name
        set(value) {
            require(Server.NAME_REGEX.matches(value)) { "Invalid name: must match the pattern ${Server.NAME_REGEX.pattern}" }
            field = value
            transaction {
                ServerTable.update({ ServerTable.id eq id }) {
                    it[name] = value
                }
            }
        }

    override var host: String = host
        set(value) {
            field = value
            transaction {
                ServerTable.update({ ServerTable.id eq id }) {
                    it[host] = value
                }
            }
        }

    override var port: Int = port
        set(value) {
            check(value in 0..65535) { "Port number $port is out of valid range (0-65535)." }
            field = value
            transaction {
                ServerTable.update({ ServerTable.id eq id }) {
                    it[port] = port
                }
            }
        }

    override val snapshotTable: SnapshotTable = SnapshotTable(id).also {
        transaction {
            SchemaUtils.create(it)
        }
    }

    init {
        Server.instances += this
    }

    override fun <O> query(query: SnapshotQuery<O>, option: O): List<Snapshot> = query.query(snapshotTable, option)

    override fun remove() {
        Server.instances -= this
        transaction {
            ServerTable.deleteWhere { id eq this@ServerImpl.id }
        }
    }

    override fun plusAssign(value: Snapshot) {
        transaction {
            snapshotTable.insert {
                it[timestamp] = value.timestamp
                it[version] = value.version
                it[protocol] = value.protocol
                it[onlinePlayers] = value.onlinePlayers?.toUInt()
                it[maxPlayers] = value.maxPlayers?.toUInt()
                it[ping] = value.ping?.toULong()
            }
        }
    }

    override fun minusAssign(value: Snapshot) {
        transaction {
            snapshotTable.deleteWhere { snapshotTable.timestamp eq value.timestamp }
        }
    }

    internal class BuilderImpl : Server.Builder {
        override var id: String? = null
            set(value) {
                require(value == null || Server.ID_REGEX.matches(value)) { "Invalid id: must match the pattern ${Server.ID_REGEX.pattern}" }
                field = value
            }

        override var name: String? = null
            set(value) {
                require(value == null || Server.NAME_REGEX.matches(value)) { "Invalid name: must match the pattern ${Server.NAME_REGEX.pattern}" }
                field = value
            }

        override var host: String? = null

        override var port: Int = 25565

        override fun build(): Server {
            checkNotNull(id) { "ID not set" }
            checkNotNull(name) { "Name not set" }
            checkNotNull(host) { "Host not set" }
            check(port in 0..65535) { "Port number $port is out of valid range (0-65535)." }

            transaction {
                if (ServerTable.selectAll().where { ServerTable.id eq id }.any()) {
                    throw IllegalStateException("ID is already in use: $id")
                }

                ServerTable.insert {
                    it[id] = this@BuilderImpl.id!!
                    it[name] = this@BuilderImpl.name!!
                    it[host] = this@BuilderImpl.host!!
                    it[port] = this@BuilderImpl.port
                }
            }

            return ServerImpl(id!!, name!!, host!!, port)
        }
    }
}