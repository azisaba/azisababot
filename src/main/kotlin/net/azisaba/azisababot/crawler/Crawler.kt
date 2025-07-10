package net.azisaba.azisababot.crawler

import net.azisaba.azisababot.minecraft
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.server.status.ServerStatus
import net.azisaba.azisababot.server.snapshots.ServerSnapshot

suspend fun crawl(server: Server, timestamp: Long = System.currentTimeMillis(), saveToDatabase: Boolean = false): ServerSnapshot {
    var status: ServerStatus? = null
    var ping: Long? = null

    println(server.serverId + "をクロール")
    println(server.endpoints.size.toString() + "個のエンドポイント")
    for (endpoint in server.endpoints) {
        println(endpoint.toString() + "をクロール")
        minecraft.fetchServerStatus(endpoint)?.let {
            status = it
        }
        minecraft.fetchPing(endpoint)?.let {
            ping = it
        }
        if (status != null && ping != null) break
    }

    val snapshot = ServerSnapshot.snapshot(timestamp, status, ping)
    if (saveToDatabase) {
        server.snapshots += snapshot
    }
    return snapshot
}