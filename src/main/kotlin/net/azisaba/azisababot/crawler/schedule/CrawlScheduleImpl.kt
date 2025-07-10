package net.azisaba.azisababot.crawler.schedule

import com.cronutils.model.Cron
import com.cronutils.model.time.ExecutionTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.azisaba.azisababot.crawler.crawl
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
    override val name: String,
    override val cron: Cron,
    override val group: ServerGroup
) : CrawlSchedule {
    private val executionTime: ExecutionTime = ExecutionTime.forCron(cron)

    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    private var future: ScheduledFuture<*>? = null

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        CrawlSchedule.instances += this
        scheduleNext()
    }

    override fun appNotation(): String = "$name (`${cron.asString()}`)"

    override fun remove() {
        future?.cancel(true)
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

        future = scheduler.schedule({
            runTask()
            scheduleNext()
        }, delay, TimeUnit.MILLISECONDS)
    }

    private fun runTask() {
        coroutineScope.launch {
            for (server in group) {
                crawl(server, timestamp(), true)
            }
        }
    }

    internal class BuilderImpl : CrawlSchedule.Builder {
        override var name: String? = null

        override var cron: Cron? = null

        override var group: ServerGroup? = null

        override fun build(): CrawlSchedule {
            checkNotNull(name) { "Name not set" }
            checkNotNull(cron) { "Cron not set" }
            check(CrawlSchedule.NAME_PATTERN.matches(name!!)) { "Invalid name: must match the pattern ${CrawlSchedule.NAME_PATTERN.pattern}" }

            transaction {
                check(CrawlScheduleTable.selectAll().where { CrawlScheduleTable.name eq this@BuilderImpl.name!! }.none()) {
                    "Name is already in use: ${this@BuilderImpl.name}"
                }

                CrawlScheduleTable.insert {
                    it[name] = this@BuilderImpl.name!!
                    it[cron] = this@BuilderImpl.cron!!.asString()
                    it[group] = this@BuilderImpl.group?.uuid
                }
            }
            return CrawlScheduleImpl(name!!, cron!!, group ?: ServerGroup.all())
        }
    }
}