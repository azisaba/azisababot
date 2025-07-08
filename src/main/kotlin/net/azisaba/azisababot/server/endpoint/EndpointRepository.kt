package net.azisaba.azisababot.server.endpoint

import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.minus
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.plus
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

internal class EndpointRepository(private val endpointTable: EndpointTable) {
    fun insert(endpoint: Endpoint, priority: Int) = transaction {
        endpointTable.insert {
            it[host] = endpoint.host
            it[port] = endpoint.port
            it[endpointTable.priority] = priority
        }
    }

    fun update(index: Int, endpoint: Endpoint) = transaction {
        endpointTable.update({ endpointTable.priority eq index }) {
            it[host] = endpoint.host
            it[port] = endpoint.port
        }
    }

    fun delete(index: Int) = transaction {
        endpointTable.deleteWhere { endpointTable.priority eq index }
        endpointTable.update({ endpointTable.priority greater index }) {
            it.update(endpointTable.priority, endpointTable.priority - 1)
        }
    }

    fun deleteIndexes(indexes: List<Int>) = transaction {
        val sorted = indexes.sorted()
        sorted.forEachIndexed { i, index ->
            endpointTable.deleteWhere { endpointTable.priority eq index }
        }
        val minIndex = sorted.minOrNull()
        if (minIndex != null) {
            endpointTable.update({ endpointTable.priority greater sorted.last() }) {
                it.update(endpointTable.priority, endpointTable.priority - sorted.size)
            }
        }
    }

    fun shiftPriorities(fromIndex: Int, delta: Int) = transaction {
        endpointTable.update({ endpointTable.priority greaterEq fromIndex }) {
            it.update(endpointTable.priority, endpointTable.priority + delta)
        }
    }

    fun selectAll(): List<Endpoint> = transaction {
        endpointTable.selectAll()
            .orderBy(endpointTable.priority)
            .map {
                Endpoint.endpoint(
                    host = it[endpointTable.host],
                    port = it[endpointTable.port]
                )
            }
    }

    fun deleteAll() = transaction {
        endpointTable.deleteAll()
    }
}