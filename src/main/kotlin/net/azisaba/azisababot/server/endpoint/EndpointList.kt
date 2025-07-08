package net.azisaba.azisababot.server.endpoint

internal class EndpointList(
    private val repository: EndpointRepository,
    private val endpoints: MutableList<Endpoint> = repository.selectAll().toMutableList()
) : MutableList<Endpoint> by endpoints {
    override fun set(index: Int, element: Endpoint): Endpoint {
        val old = endpoints.set(index, element)
        repository.update(index, element)
        return old
    }

    override fun add(element: Endpoint): Boolean {
        add(endpoints.size, element)
        return true
    }

    override fun add(index: Int, element: Endpoint) {
        repository.shiftPriorities(index, 1)
        endpoints.add(index, element)
        repository.insert(element, index)
    }

    override fun addAll(index: Int, elements: Collection<Endpoint>): Boolean {
        if (elements.isEmpty()) return false
        repository.shiftPriorities(index, elements.size)
        endpoints.addAll(index, elements)
        elements.withIndex().forEach { (offset, endpoint) ->
            repository.insert(endpoint, index + offset)
        }
        return true
    }

    override fun remove(element: Endpoint): Boolean {
        val index = endpoints.indexOf(element)
        if (index >= 0) {
            removeAt(index)
            return true
        }
        return false
    }

    override fun removeAt(index: Int): Endpoint {
        val removed = endpoints.removeAt(index)
        repository.delete(index)
        return removed
    }

    override fun removeAll(elements: Collection<Endpoint>): Boolean {
        val indexesToRemove = endpoints.mapIndexedNotNull { index, endpoint ->
            if (endpoint in elements) index else null
        }.sortedDescending()

        indexesToRemove.forEach { index -> endpoints.removeAt(index) }
        repository.deleteIndexes(indexesToRemove)

        return indexesToRemove.isNotEmpty()
    }

    override fun retainAll(elements: Collection<Endpoint>): Boolean {
        val indexesToRemove = endpoints.mapIndexedNotNull { index, endpoint ->
            if (endpoint !in elements) index else null
        }.sortedDescending()

        indexesToRemove.forEach { index ->
            removeAt(index)
        }

        return indexesToRemove.isNotEmpty()
    }

    override fun clear() {
        repository.deleteAll()
        endpoints.clear()
    }
}