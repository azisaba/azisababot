package net.azisaba.azisababot.server.endpoints

import net.azisaba.azisababot.server.Server
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.plus
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal class EndpointRepositoryImpl(private val server: Server) : EndpointRepository {
    internal val table: EndpointTable = EndpointTable(server).also {
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
            .orderBy(table.priority)
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