package net.azisaba.azisababot.crawler.schedule

import net.azisaba.azisababot.server.ServerTable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table

object CrawlScheduleTable : Table("crawl_schedule") {
    val id: Column<String> = varchar("id", 16)

    val cron: Column<String> = varchar("cron", 86)

    val target: Column<String?> = varchar("target", 16).references(ServerTable.id).nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}