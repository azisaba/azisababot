package net.azisaba.azisababot.crawler.schedule

import net.azisaba.azisababot.server.group.ServerGroupTable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import java.util.*

object CrawlScheduleTable : Table("schedule") {
    val name: Column<String> = varchar("name", 16)

    val cron: Column<String> = varchar("cron", 86)

    val group: Column<UUID?> = uuid("group").references(ServerGroupTable.uuid).nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(name)
}