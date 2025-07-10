package net.azisaba.azisababot.server

import net.azisaba.azisababot.server.group.ServerGroup
import net.azisaba.azisababot.server.snapshot.SnapshotsImpl
import net.azisaba.azisababot.server.endpoints.EndpointRepositoryImpl
import net.azisaba.azisababot.server.endpoints.EndpointsImpl
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.net.InetSocketAddress
import java.util.*

internal class ServerImpl(
    override val uuid: UUID,
    serverId: String,
    displayName: String
) : Server {
    override var serverId: String = serverId
        set(value) {
            require(Server.SERVER_ID_REGEX.matches(value)) { "Invalid server ID: must match the pattern ${Server.SERVER_ID_REGEX.pattern}" }
            field = value
            transaction {
                if (ServerTable.selectAll().where { ServerTable.serverId eq value }.any()) {
                    throw IllegalStateException("This server ID is already in use")
                }

                ServerTable.update({ ServerTable.uuid eq uuid }) {
                    it[serverId] = value
                }
            }
        }

    override var displayName: String = displayName
        set(value) {
            require(Server.DISPLAY_NAME_REGEX.matches(value)) { "Invalid display name: must match the pattern ${Server.DISPLAY_NAME_REGEX.pattern}" }
            field = value
            transaction {
                ServerTable.update({ ServerTable.uuid eq uuid }) {
                    it[displayName] = value
                }
            }
        }

    override val endpoints: EndpointsImpl = EndpointsImpl(EndpointRepositoryImpl(this))

    override val snapshots: SnapshotsImpl = SnapshotsImpl(this)

    init {
        Server.instances += this
    }

    override fun appNotation(): String = "$displayName (`$serverId`)"

    override fun remove() {
        Server.instances -= this

        ServerGroup.groups(this).forEach { group ->
            group -= this
        }

        transaction {
            ServerTable.deleteWhere { uuid eq this@ServerImpl.uuid }
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

        override var displayName: String? = null

        override fun build(): Server {
            checkNotNull(serverId) { "Server ID not set" }
            checkNotNull(displayName) { "Name not set" }

            check(Server.SERVER_ID_REGEX.matches(serverId!!)) {
                "Invalid server ID: must match the pattern ${Server.SERVER_ID_REGEX.pattern}"
            }

            check(Server.DISPLAY_NAME_REGEX.matches(displayName!!)) {
                "Invalid display name: must match the pattern ${Server.DISPLAY_NAME_REGEX.pattern}"
            }

            transaction {
                check(ServerTable.selectAll().where { ServerTable.uuid eq uuid }.none()) {
                    "UUID is already in use: $uuid"
                }

                check(ServerTable.selectAll().where { ServerTable.serverId eq serverId!! }.none()) {
                    "Server ID is already in use: $serverId"
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