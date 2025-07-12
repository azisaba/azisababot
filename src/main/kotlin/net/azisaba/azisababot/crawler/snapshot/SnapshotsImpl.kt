package net.azisaba.azisababot.crawler.snapshot

import com.cronutils.model.Cron
import com.cronutils.model.time.ExecutionTime
import net.azisaba.azisababot.cronParser
import net.azisaba.azisababot.server.Server
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.between
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

internal class SnapshotsImpl(override val server: Server) : Snapshots {
    override val size: Long
        get() = transaction {
            snapshotTable.selectAll().count()
        }

    private val snapshotTable: SnapshotTable = SnapshotTable(server).also {
        transaction {
            SchemaUtils.create(it)
        }
    }

    override fun plusAssign(snapshot: Snapshot) {
        transaction {
            snapshotTable.insert {
                it[timestamp] = snapshot.timestamp
                it[version] = snapshot.status?.version
                it[protocol] = snapshot.status?.protocol
                it[onlinePlayers] = snapshot.status?.onlinePlayers
                it[maxPlayers] = snapshot.status?.maxPlayers
                it[favicon] = snapshot.status?.favicon
                it[description] = snapshot.status?.description?.let { GsonComponentSerializer.gson().serialize(it) }
                it[ping] = snapshot.ping
            }
        }
    }

    override fun minusAssign(snapshot: Snapshot) {
        transaction {
            snapshotTable.deleteWhere { snapshotTable.timestamp eq snapshot.timestamp }
        }
    }

    override fun query(): Snapshots.Query = QueryImpl()

    override fun clear() {
        transaction {
            snapshotTable.deleteAll()
        }
    }

    override fun drop() {
        Snapshots.instances -= server
        transaction {
            SchemaUtils.drop(snapshotTable)
        }
    }

    private inner class QueryImpl : Snapshots.Query {
        private var versionFilter: String? = null

        private var minPlayers: Int? = null
        private var maxPlayers: Int? = null

        private var cron: Cron? = null

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

        override fun cron(cron: Cron): Snapshots.Query = apply {
            this.cron = cron
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
                conditions += snapshotTable.version eq it
            }

            minPlayers?.let {
                conditions += snapshotTable.onlinePlayers greaterEq it
            }

            maxPlayers?.let {
                conditions += snapshotTable.onlinePlayers lessEq it
            }

            startTime?.let { start -> endTime?.let { end ->
                conditions += snapshotTable.timestamp.between(start, end)
            } }

            afterTime?.let {
                conditions += snapshotTable.timestamp greaterEq it
            }

            beforeTime?.let {
                conditions += snapshotTable.timestamp lessEq it
            }

            val baseQuery = if (conditions.isNotEmpty()) {
                snapshotTable.selectAll().where { conditions.reduce { acc, op -> acc and op } }
            } else {
                snapshotTable.selectAll()
            }

            val ordered = baseQuery.orderBy(snapshotTable.timestamp, if (orderDesc) SortOrder.DESC else SortOrder.ASC)
            val limited = limit?.let { ordered.limit(it) } ?: ordered

            val raw = limited.map { Snapshot.snapshot(it, snapshotTable) }

            val filtered = if (cron != null) {
                val execTime = ExecutionTime.forCron(cron)
                raw.filter {
                    val zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(it.timestamp), ZoneId.systemDefault())
                    execTime.isMatch(zonedDateTime)
                }
            } else raw

            return@transaction QueryResultImpl(filtered)
        }
    }

    private class QueryResultImpl(private val snapshots: List<Snapshot>) : Snapshots.QueryResult {
        override fun averagePing(): Double? = snapshots.mapNotNull { it.ping }.average().takeIf { !it.isNaN() }

        override fun averagePlayers(): Double? = snapshots.mapNotNull { it.status?.onlinePlayers }.average().takeIf { !it.isNaN() }

        override fun minPing(): Long? = snapshots.mapNotNull { it.ping }.minOrNull()

        override fun maxPing(): Long? = snapshots.mapNotNull { it.ping }.maxOrNull()

        override fun iterator(): Iterator<Snapshot> = snapshots.iterator()
    }
}