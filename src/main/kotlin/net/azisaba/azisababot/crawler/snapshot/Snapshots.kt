package net.azisaba.azisababot.crawler.snapshot

interface Snapshots {
    operator fun plusAssign(snapshot: Snapshot)

    operator fun minusAssign(snapshot: Snapshot)

    fun query(): Query

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

    interface QueryResult {
        val snapshots: List<Snapshot>

        fun averagePing(): Double?

        fun averagePlayers(): Double?

        fun minPing(): Long?

        fun maxPing(): Long?
    }
}