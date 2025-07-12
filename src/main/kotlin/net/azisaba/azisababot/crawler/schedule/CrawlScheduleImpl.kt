package net.azisaba.azisababot.crawler.schedule

import com.cronutils.model.Cron
import com.cronutils.model.time.ExecutionTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.azisaba.azisababot.crawler.crawl
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.server.group.ServerGroup
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

internal data class CrawlScheduleImpl(
    override val id: String,
    override val cron: Cron,
    override val target: ServerGroup?
) : CrawlSchedule {
    private val executionTime: ExecutionTime = ExecutionTime.forCron(cron)

    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    private var scheduledFuture: ScheduledFuture<*>? = null

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        CrawlSchedule.instances += this
        scheduleNext()
    }

    override fun cancel() {
        scheduledFuture?.cancel(true)
        scheduler.shutdownNow()
        CrawlSchedule.instances -= this
        transaction {
            CrawlScheduleTable.deleteWhere { cron eq this@CrawlScheduleImpl.cron.asString() }
        }
    }

    private fun timestamp(): Long {
        val now = ZonedDateTime.now()
        return ExecutionTime.forCron(cron).lastExecution(now)
            .or { ExecutionTime.forCron(cron).nextExecution(now) }
            .map { it.toInstant().toEpochMilli() }
            .orElse(System.currentTimeMillis())
    }

    private fun scheduleNext() {
        val now = ZonedDateTime.now()
        val nextExecution = executionTime.nextExecution(now).orElse(null) ?: return

        val delay = Duration.between(now, nextExecution).toMillis()

        scheduledFuture = scheduler.schedule({
            runTask()
            scheduleNext()
        }, delay, TimeUnit.MILLISECONDS)
    }

    private fun runTask() {
        coroutineScope.launch {
            for (server in target ?: Server.servers()) {
                crawl(server, timestamp(), true)
            }
        }
    }

    internal class BuilderImpl : CrawlSchedule.Builder {
        override var id: String? = null

        override var cron: Cron? = null

        override var group: ServerGroup? = null

        override fun build(): CrawlSchedule {
            checkNotNull(id) { "ID not set" }
            checkNotNull(cron) { "Cron not set" }
            check(CrawlSchedule.NAME_PATTERN.matches(id!!)) { "Invalid name: must match the pattern ${CrawlSchedule.NAME_PATTERN.pattern}" }

            transaction {
                check(CrawlScheduleTable.selectAll().where { CrawlScheduleTable.id eq this@BuilderImpl.id!! }.none()) {
                    "ID is already in use: ${this@BuilderImpl.id}"
                }

                CrawlScheduleTable.insert {
                    it[id] = this@BuilderImpl.id!!
                    it[cron] = this@BuilderImpl.cron!!.asString()
                    it[target] = this@BuilderImpl.group?.id
                }
            }

            return CrawlScheduleImpl(id!!, cron!!, group)
        }
    }
}