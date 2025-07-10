package net.azisaba.azisababot.server.snapshot

import com.cronutils.model.time.ExecutionTime
import net.azisaba.azisababot.cronParser
import net.azisaba.azisababot.server.Server
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.between
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

internal class SnapshotsImpl(private val server: Server) : Snapshots {
    internal val table: SnapshotTable = SnapshotTable(server).also {
        transaction {
            SchemaUtils.create(it)
        }
    }

    override fun plusAssign(snapshot: Snapshot) {
        transaction {
            table.insert {
                it[timestamp] = snapshot.timestamp
                it[version] = snapshot.status?.version
                it[protocol] = snapshot.status?.protocol
                it[onlinePlayers] = snapshot.status?.onlinePlayers
                it[maxPlayers] = snapshot.status?.maxPlayers
                it[favicon] = snapshot.status?.favicon
                it[ping] = snapshot.ping
            }
        }
    }

    override fun minusAssign(snapshot: Snapshot) {
        transaction {
            table.deleteWhere { table.timestamp eq snapshot.timestamp }
        }
    }

    override fun query(): Snapshots.Query = QueryImpl()

    private inner class QueryImpl : Snapshots.Query {
        private var versionFilter: String? = null

        private var minPlayers: Int? = null
        private var maxPlayers: Int? = null

        private var cronExpression: String? = null

        private var orderDesc: Boolean = true

        private var limit: Int? = null

        private var afterTime: Long? = null
        private var beforeTime: Long? = null

        private var startTime: Long? = null
        private var endTime: Long? = null

        override fun version(version: String): Snapshots.Query = apply {
            versionFilter = version
        }

        override fun minPlayers(minPlayers: Int): Snapshots.Query = apply {
            this.minPlayers = minPlayers
        }

        override fun maxPlayers(maxPlayers: Int): Snapshots.Query = apply {
            this.maxPlayers = maxPlayers
        }

        override fun cron(cronExpression: String): Snapshots.Query = apply {
            this.cronExpression = cronExpression
        }

        override fun orderByTimestamp(descending: Boolean): Snapshots.Query = apply {
            orderDesc = descending
        }

        override fun limit(limit: Int) = apply {
            this.limit = limit
        }

        override fun after(timestamp: Long): Snapshots.Query = apply {
            afterTime = timestamp
        }

        override fun before(timestamp: Long): Snapshots.Query = apply {
            beforeTime = timestamp
        }

        override fun timeRange(start: Long, end: Long): Snapshots.Query = apply {
            startTime = start
            endTime = end
        }

        override fun execute(): Snapshots.QueryResult = transaction {
            val conditions = mutableListOf<Op<Boolean>>()

            versionFilter?.let {
                conditions += table.version eq it
            }

            minPlayers?.let {
                conditions += table.onlinePlayers greaterEq it
            }

            maxPlayers?.let {
                conditions += table.onlinePlayers lessEq it
            }

            startTime?.let { start -> endTime?.let { end ->
                conditions += table.timestamp.between(start, end)
            } }

            afterTime?.let {
                conditions += table.timestamp greaterEq it
            }

            beforeTime?.let {
                conditions += table.timestamp lessEq it
            }

            val baseQuery = if (conditions.isNotEmpty()) {
                table.selectAll().where { conditions.reduce { acc, op -> acc and op } }
            } else {
                table.selectAll()
            }

            val ordered = baseQuery.orderBy(table.timestamp, if (orderDesc) SortOrder.DESC else SortOrder.ASC)
            val limited = limit?.let { ordered.limit(it) } ?: ordered

            val raw = limited.map { Snapshot.snapshot(it, table) }

            val filtered = if (cronExpression != null) {
                val cron = cronParser.parse(cronExpression)?.also { it.validate() }
                val execTime = ExecutionTime.forCron(cron)
                raw.filter {
                    val zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(it.timestamp), ZoneId.systemDefault())
                    execTime.isMatch(zonedDateTime)
                }
            } else raw

            return@transaction QueryResultImpl(filtered)
        }
    }

    private class QueryResultImpl(override val snapshots: List<Snapshot>) : Snapshots.QueryResult {
        override fun averagePing(): Double? = snapshots.mapNotNull { it.ping }.average().takeIf { !it.isNaN() }

        override fun averagePlayers(): Double? = snapshots.mapNotNull { it.status?.onlinePlayers }.average().takeIf { !it.isNaN() }

        override fun minPing(): Long? = snapshots.mapNotNull { it.ping }.minOrNull()

        override fun maxPing(): Long? = snapshots.mapNotNull { it.ping }.maxOrNull()
    }
}