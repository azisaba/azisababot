package net.azisaba.azisababot.server.snapshots

interface ServerSnapshots {
    operator fun plusAssign(snapshot: ServerSnapshot)

    operator fun minusAssign(snapshot: ServerSnapshot)

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
        val snapshots: List<ServerSnapshot>

        fun averagePing(): Double?

        fun averagePlayers(): Double?

        fun minPing(): Long?

        fun maxPing(): Long?
    }
}