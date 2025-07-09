package net.azisaba.azisababot.server.endpoints

import net.azisaba.azisababot.server.Server

internal interface EndpointRepository {
    fun select(priority: Int): Server.Endpoint?

    fun selectAll(): List<Server.Endpoint>

    fun insert(endpoint: Server.Endpoint, priority: Int)

    fun delete(priority: Int)

    fun deleteAll()

    fun updatePriority(oldPriority: Int, newPriority: Int)

    fun shiftPriorities(fromIndex: Int, delta: Int)

    fun count(): Int
}