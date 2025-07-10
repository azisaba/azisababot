package net.azisaba.azisababot.server.endpoints

import net.azisaba.azisababot.server.Server

internal class EndpointsImpl(internal val repository: EndpointRepositoryImpl) : Endpoints {
    override val size: Int
        get() = repository.count()

    override fun get(priority: Int): Server.Endpoint = repository.select(priority)
        ?: throw IndexOutOfBoundsException("No endpoint at priority $priority")

    override fun set(priority: Int, endpoint: Server.Endpoint) {
        insertAt(endpoint, priority)
    }

    override fun plusAssign(endpoint: Server.Endpoint) {
        add(endpoint)
    }

    override fun plusAssign(endpoints: Iterable<Server.Endpoint>) {
        addAll(endpoints)
    }

    override fun minusAssign(priority: Int) {
        remove(priority)
    }

    override fun add(endpoint: Server.Endpoint) {
        val newPriority = repository.count()
        repository.insert(endpoint, newPriority)
    }

    override fun addAll(endpoints: Iterable<Server.Endpoint>) {
        val startPriority = repository.count()
        endpoints.forEachIndexed { offset, endpoint ->
            repository.insert(endpoint, startPriority + offset)
        }
    }

    override fun remove(priority: Int): Boolean {
        val total = repository.count()
        if (priority !in 0 until total) return false
        repository.delete(priority)
        repository.shiftPriorities(priority + 1, -1)
        return true
    }

    override fun insertAt(endpoint: Server.Endpoint, priority: Int) {
        val insertPriority = priority.coerceIn(0, repository.count())
        repository.shiftPriorities(insertPriority, 1)
        repository.insert(endpoint, insertPriority)
    }

    override fun clear() {
        repository.deleteAll()
    }

    override fun iterator(): Iterator<Server.Endpoint> = repository.selectAll().iterator()
}