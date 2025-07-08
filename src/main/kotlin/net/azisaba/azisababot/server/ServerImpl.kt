package net.azisaba.azisababot.server

import net.azisaba.azisababot.server.endpoint.Endpoint
import net.azisaba.azisababot.server.endpoint.EndpointList
import net.azisaba.azisababot.server.endpoint.EndpointRepository
import net.azisaba.azisababot.server.endpoint.EndpointTable
import net.azisaba.azisababot.server.snapshot.SnapshotTable
import net.azisaba.azisababot.server.snapshot.Snapshots
import net.azisaba.azisababot.server.snapshot.SnapshotsImpl
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.*

internal class ServerImpl(
    override val uuid: UUID,
    override var serverId: String,
    displayName: String
) : Server {
    override var displayName: String = displayName
        set(value) {
            require(Server.NAME_REGEX.matches(value)) { "Invalid name: must match the pattern ${Server.NAME_REGEX.pattern}" }
            field = value
            transaction {
                ServerTable.update({ ServerTable.serverId eq id }) {
                    it[displayName] = value
                }
            }
        }

    override val endpoints: MutableList<Endpoint> by lazy { EndpointList(EndpointRepository(endpointTable)) }

    override val snapshots: Snapshots by lazy { SnapshotsImpl(snapshotTable) }

    private val endpointTable: EndpointTable = EndpointTable("${uuid.toString().replace('-', '_')}_endpoint").also {
        transaction {
            SchemaUtils.create(it)
        }
    }

    private val snapshotTable: SnapshotTable = SnapshotTable("${uuid.toString().replace('-', '_')}_snapshot").also {
        transaction {
            SchemaUtils.create(it)
        }
    }

    init {
        Server.instances += this
    }

    override fun remove() {
        Server.instances -= this
        transaction {
            ServerTable.deleteWhere { serverId eq this@ServerImpl.serverId }
            SchemaUtils.drop(endpointTable)
        }
    }

    internal class BuilderImpl : Server.Builder {
        override var serverId: String? = null
            set(value) {
                require(value == null || Server.ID_REGEX.matches(value)) { "Invalid id: must match the pattern ${Server.ID_REGEX.pattern}" }
                field = value
            }

        override var displayName: String? = null
            set(value) {
                require(value == null || Server.NAME_REGEX.matches(value)) { "Invalid name: must match the pattern ${Server.NAME_REGEX.pattern}" }
                field = value
            }

        override val endpoints: MutableList<Endpoint> = mutableListOf()

        private val uuid: UUID = UUID.randomUUID()

        override fun build(): Server {
            checkNotNull(serverId) { "ID not set" }
            checkNotNull(displayName) { "Name not set" }

            transaction {
                if (ServerTable.selectAll().where { ServerTable.serverId eq id }.any()) {
                    throw IllegalStateException("ID is already in use: $id")
                }

                ServerTable.insert {
                    it[uuid] = this@BuilderImpl.uuid
                    it[serverId] = this@BuilderImpl.serverId!!
                    it[displayName] = this@BuilderImpl.displayName!!
                }
            }

            return ServerImpl(uuid, serverId!!, displayName!!).apply {
                endpoints.addAll(this@BuilderImpl.endpoints)
            }
        }
    }
}