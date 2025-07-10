package net.azisaba.azisababot.server.endpoints

import net.azisaba.azisababot.server.Server

internal interface ServerEndpointRepository {
    fun select(priority: Int): Server.Endpoint?

    fun selectAll(): List<Server.Endpoint>

    fun insert(endpoint: Server.Endpoint, priority: Int)

    fun delete(priority: Int)

    fun delete(endpoints: Iterable<Server.Endpoint>)

    fun deleteAll()

    fun updatePriority(oldPriority: Int, newPriority: Int)

    fun shiftPriorities(fromIndex: Int, delta: Int)

    fun count(): Int
}