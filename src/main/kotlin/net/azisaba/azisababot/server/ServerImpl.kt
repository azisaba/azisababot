package net.azisaba.azisababot.server

import net.azisaba.azisababot.crawler.snapshot.Snapshots
import net.azisaba.azisababot.server.group.ServerGroup
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.net.InetSocketAddress

internal class ServerImpl(
    override val id: String,
    name: String?
) : Server {
    override var name: String? = name
        set(value) {
            require(value == null || Server.NAME_REGEX.matches(value)) { "Invalid name: must match the pattern ${Server.NAME_REGEX.pattern}" }
            field = value
            transaction {
                ServerTable.update({ ServerTable.id eq id }) {
                    it[name] = value
                }
            }
        }

    private val endpoints: MutableMap<Server.Endpoint, Int> = mutableMapOf()

    private val endpointTable: EndpointTable = EndpointTable(this).also {
        transaction {
            SchemaUtils.create(it)
        }
    }

    init {
        Server.instances += this
    }

    override fun get(key: Server.Endpoint): Int? = endpoints[key]

    override fun set(key: Server.Endpoint, value: Int?) {
        if (value != null) {
            endpoints[key] = value
            transaction {
                endpointTable.upsert {
                    it[host] = key.host
                    it[port] = key.port
                    it[priority] = value
                }
            }
        } else if (endpoints.remove(key) != null) {
            transaction {
                endpointTable.deleteWhere { (host eq key.host) and (port eq key.port) }
            }
        }
    }

    override fun contains(endpoint: Server.Endpoint): Boolean = endpoint in endpoints

    override fun iterator(): Iterator<Pair<Int, Server.Endpoint>> = endpoints.map { it.value to it.key }
        .sortedBy { it.second.toString() }
        .sortedByDescending { it.first }
        .iterator()

    override fun clear() {
        endpoints.clear()
        transaction {
            endpointTable.deleteAll()
        }
    }

    override fun remove() {
        Server.instances -= this

        Snapshots.of(this).drop()

        ServerGroup.groups(this).forEach { group ->
            group -= this
        }

        transaction {
            SchemaUtils.drop(endpointTable)
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
        override var id: String? = null

        override var name: String? = null

        override fun build(): Server {
            checkNotNull(id) { "ID not set" }

            check(Server.ID_REGEX.matches(id!!)) {
                "Invalid ID: must match the pattern ${Server.ID_REGEX.pattern}"
            }

            check(name == null || Server.NAME_REGEX.matches(name!!)) {
                "Invalid name: must match the pattern ${Server.NAME_REGEX.pattern}"
            }

            transaction {
                check(ServerTable.selectAll().where { ServerTable.id eq this@BuilderImpl.id!! }.none()) {
                    "ID is already in use: ${this@BuilderImpl.id}"
                }

                ServerTable.insert {
                    it[id] = this@BuilderImpl.id!!
                    it[name] = this@BuilderImpl.name
                }
            }

            return ServerImpl(id!!, name)
        }
    }
}