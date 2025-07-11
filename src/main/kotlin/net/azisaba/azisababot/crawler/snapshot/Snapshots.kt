package net.azisaba.azisababot.crawler.snapshot

import net.azisaba.azisababot.server.Server

interface Snapshots {
    val size: Long

    val server: Server

    operator fun plusAssign(snapshot: Snapshot)

    operator fun minusAssign(snapshot: Snapshot)

    fun query(): Query

    fun clear()

    fun drop()

    companion object {
        internal val instances: MutableMap<Server, Snapshots> = mutableMapOf()

        fun of(server: Server): Snapshots = instances[server] ?: SnapshotsImpl(server)
    }

    interface Query {
        fun version(version: String): Query

        fun minPlayers(minPlayers: Int): Query

        fun maxPlayers(maxPlayers: Int): Query

        fun cron(cronExpression: String): Query

        fun orderByTimestamp(descending: Boolean = true): Query

        fun limit(limit: Int): Query

        fun after(timestamp: Long): Query

        fun before(timestamp: Long): Query

        fun timeRange(start: Long, end: Long): Query

        fun execute(): QueryResult
    }

    interface QueryResult : Iterable<Snapshot> {
        fun averagePing(): Double?

        fun averagePlayers(): Double?

        fun minPing(): Long?

        fun maxPing(): Long?
    }
}