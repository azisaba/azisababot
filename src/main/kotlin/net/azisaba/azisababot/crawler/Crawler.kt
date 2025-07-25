package net.azisaba.azisababot.crawler

import net.azisaba.azisababot.crawler.snapshot.Snapshot
import net.azisaba.azisababot.crawler.snapshot.Snapshots
import net.azisaba.azisababot.minecraft
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.server.ServerStatus

suspend fun crawl(server: Server, timestamp: Long = System.currentTimeMillis(), saveToDatabase: Boolean = false): Snapshot {
    var status: ServerStatus? = null
    var ping: Long? = null

    for ((_, endpoint) in server) {
        minecraft.serverStatus(endpoint)?.let {
            status = it
        }
        minecraft.ping(endpoint)?.let {
            ping = it
        }
        if (status != null && ping != null) break
    }

    val snapshots = Snapshots.snapshots(server)
    val snapshot = Snapshot.snapshot(timestamp, status, ping)
    if (saveToDatabase) {
        snapshots += snapshot
    }
    return snapshot
}