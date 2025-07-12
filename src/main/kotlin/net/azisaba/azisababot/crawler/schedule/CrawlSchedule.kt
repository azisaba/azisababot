package net.azisaba.azisababot.crawler.schedule

import com.cronutils.model.Cron
import net.azisaba.azisababot.Identified
import net.azisaba.azisababot.cronParser
import net.azisaba.azisababot.server.group.ServerGroup
import org.jetbrains.exposed.v1.core.ResultRow

interface CrawlSchedule : Identified {
    val cron: Cron

    val target: ServerGroup?

    fun cancel()

    companion object {
        val NAME_PATTERN: Regex = Regex("^[a-z0-9_]{1,16}$")

        internal val instances: MutableSet<CrawlSchedule> = mutableSetOf()

        fun schedule(id: String): CrawlSchedule? = instances.find { it.id == id }

        fun schedule(block: Builder.() -> Unit): CrawlSchedule = CrawlScheduleImpl.BuilderImpl().apply(block).build()

        fun schedules(): Set<CrawlSchedule> = instances.sortedBy { it.id }.toSet()

        internal fun load(row: ResultRow): CrawlSchedule = CrawlScheduleImpl(
            id = row[CrawlScheduleTable.id],
            cron = row[CrawlScheduleTable.cron].let { cronParser.parse(it) },
            target = row[CrawlScheduleTable.target]?.let { ServerGroup.group(it) }
        )
    }

    @CrawlScheduleDsl
    interface Builder {
        var id: String?

        var cron: Cron?

        var group: ServerGroup?

        fun build(): CrawlSchedule
    }
}

@DslMarker
annotation class CrawlScheduleDsl()