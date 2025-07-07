package net.azisaba.azisababot.util

data class UnixTimeRange(val start: Long, val end: Long) {
    init {
        require(start <= end) { "start must be <= end" }
    }

    operator fun contains(time: Long): Boolean = time in start..end
}
