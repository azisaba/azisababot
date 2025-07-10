package net.azisaba.azisababot.server.endpoints

import net.azisaba.azisababot.server.Server

interface Endpoints : Iterable<Server.Endpoint> {
    val size: Int

    operator fun get(priority: Int): Server.Endpoint

    operator fun set(priority: Int, endpoint: Server.Endpoint)

    operator fun plusAssign(endpoint: Server.Endpoint)

    operator fun plusAssign(endpoints: Iterable<Server.Endpoint>)

    operator fun minusAssign(priority: Int)

    fun add(endpoint: Server.Endpoint)

    fun addAll(endpoints: Iterable<Server.Endpoint>)

    fun remove(priority: Int): Boolean

    fun removeAll(endpoints: Iterable<Server.Endpoint>): Boolean

    fun insertAt(endpoint: Server.Endpoint, priority: Int)

    fun move(fromPriority: Int, toPriority: Int)

    fun clear()
}