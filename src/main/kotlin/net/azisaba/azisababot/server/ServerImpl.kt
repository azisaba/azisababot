package net.azisaba.azisababot.server

import net.azisaba.azisababot.crawler.snapshot.SnapshotsImpl
import net.azisaba.azisababot.server.endpoints.EndpointRepositoryImpl
import net.azisaba.azisababot.server.endpoints.EndpointsImpl
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.net.InetSocketAddress
import java.util.*

internal class ServerImpl(
    override val uuid: UUID,
    override var serverId: String,
    displayName: String
) : Server {
    override var displayName: String = displayName
        set(value) {
            require(Server.DISPLAY_NAME_REGEX.matches(value)) { "Invalid name: must match the pattern ${Server.DISPLAY_NAME_REGEX.pattern}" }
            field = value
            transaction {
                ServerTable.update({ ServerTable.serverId eq id }) {
                    it[displayName] = value
                }
            }
        }

    override val endpoints: EndpointsImpl = EndpointsImpl(EndpointRepositoryImpl(this))

    override val snapshots: SnapshotsImpl = SnapshotsImpl(this)

    init {
        Server.instances += this
    }

    override fun remove() {
        Server.instances -= this
        transaction {
            ServerTable.deleteWhere { serverId eq this@ServerImpl.serverId }
            SchemaUtils.drop(endpoints.repository.table)
            SchemaUtils.drop(snapshots.table)
        }
    }

    internal data class EndpointImpl(
        override val host: String,
        override val port: Int
    ) : Server.Endpoint {
        override fun toINetSocketAddress(): InetSocketAddress = InetSocketAddress.createUnresolved(host, port)

        override fun toString(): String = "$host:$port"
    }

    internal class BuilderImpl : Server.Builder {
        override var uuid: UUID = UUID.randomUUID()

        override var serverId: String? = null
            set(value) {
                require(value == null || Server.SERVER_ID_REGEX.matches(value)) { "Invalid id: must match the pattern ${Server.SERVER_ID_REGEX.pattern}" }
                field = value
            }

        override var displayName: String? = null
            set(value) {
                require(value == null || Server.DISPLAY_NAME_REGEX.matches(value)) { "Invalid name: must match the pattern ${Server.DISPLAY_NAME_REGEX.pattern}" }
                field = value
            }

        override fun build(): Server {
            checkNotNull(serverId) { "Server ID not set" }
            checkNotNull(displayName) { "Name not set" }

            transaction {
                if (ServerTable.selectAll().where { ServerTable.uuid eq uuid }.any()) {
                    throw IllegalStateException("UUID is already in use: $uuid")
                }

                if (ServerTable.selectAll().where { ServerTable.serverId eq serverId!! }.any()) {
                    throw IllegalStateException("Server ID is already in use: $serverId")
                }

                ServerTable.insert {
                    it[uuid] = this@BuilderImpl.uuid
                    it[serverId] = this@BuilderImpl.serverId!!
                    it[displayName] = this@BuilderImpl.displayName!!
                }
            }

            return ServerImpl(uuid, serverId!!, displayName!!)
        }
    }
}