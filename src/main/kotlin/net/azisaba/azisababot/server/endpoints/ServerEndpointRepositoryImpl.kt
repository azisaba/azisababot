package net.azisaba.azisababot.server.endpoints

import net.azisaba.azisababot.server.Server
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.inList
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.plus
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal class ServerEndpointRepositoryImpl(private val server: Server) : ServerEndpointRepository {
    internal val table: ServerEndpointTable = ServerEndpointTable(server).also {
        transaction {
            SchemaUtils.create(it)
        }
    }

    override fun select(priority: Int): Server.Endpoint? = transaction {
        table.selectAll()
            .where { table.priority eq priority }
            .firstOrNull()
            ?.let {
                Server.Endpoint.of(it[table.host], it[table.port])
            }
    }

    override fun selectAll(): List<Server.Endpoint> = transaction {
        table.selectAll()
            .orderBy(table.priority to SortOrder.DESC)
            .map {
                Server.Endpoint.of(it[table.host], it[table.port])
            }
    }

    override fun insert(endpoint: Server.Endpoint, priority: Int): Unit = transaction {
        table.insert {
            it[host] = endpoint.host
            it[port] = endpoint.port
            it[this.priority] = priority
        }
    }

    override fun delete(priority: Int): Unit = transaction {
        table.deleteWhere { table.priority eq priority }
    }

    override fun delete(endpoints: Iterable<Server.Endpoint>) = transaction {
        val pairs = endpoints.map { it.host to it.port }.toSet()

        table.deleteWhere {
            (table.host to table.port) inList pairs
        }

        val remaining = table.selectAll().orderBy(table.priority).toList()
        remaining.forEachIndexed { index, row ->
            table.update({ table.priority eq row[table.priority] }) {
                it[priority] = index
            }
        }
    }

    override fun deleteAll(): Unit = transaction {
        table.deleteAll()
    }

    override fun updatePriority(oldPriority: Int, newPriority: Int): Unit = transaction {
        table.update({ table.priority eq oldPriority }) {
            it[priority] = newPriority
        }
    }

    override fun shiftPriorities(fromIndex: Int, delta: Int) = transaction {
        if (delta == 0) return@transaction
        table.update({ table.priority greaterEq fromIndex }) {
            it.update(table.priority, table.priority + delta)
        }
    }

    override fun count(): Int = transaction {
        table.selectAll().count().toInt()
    }
}