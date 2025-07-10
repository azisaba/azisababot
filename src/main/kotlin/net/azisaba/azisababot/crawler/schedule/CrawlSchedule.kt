package net.azisaba.azisababot.crawler.schedule

import com.cronutils.model.Cron
import net.azisaba.azisababot.cronParser
import net.azisaba.azisababot.server.group.ServerGroup
import org.jetbrains.exposed.v1.core.ResultRow

interface CrawlSchedule {
    val name: String

    val cron: Cron

    val group: ServerGroup

    fun appNotation(): String

    fun remove()

    companion object {
        val NAME_PATTERN: Regex = Regex("^[a-z0-9_]{1,16}$")

        internal val instances: MutableSet<CrawlSchedule> = mutableSetOf()

        fun schedule(name: String): CrawlSchedule? = instances.find { it.name == name }

        fun schedule(block: Builder.() -> Unit): CrawlSchedule = CrawlScheduleImpl.BuilderImpl().apply(block).build()

        fun schedules(): Set<CrawlSchedule> = instances.toSet()

        internal fun load(row: ResultRow): CrawlSchedule = CrawlScheduleImpl(
            name = row[CrawlScheduleTable.name],
            cron = cronParser.parse(row[CrawlScheduleTable.cron]),
            group = row[CrawlScheduleTable.group]?.let { ServerGroup.group(it) } ?: ServerGroup.all()
        )
    }

    @CrawlScheduleDsl
    interface Builder {
        var name: String?

        var cron: Cron?

        var group: ServerGroup?

        fun build(): CrawlSchedule
    }
}

@DslMarker
annotation class CrawlScheduleDsl()