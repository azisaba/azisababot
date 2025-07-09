package net.azisaba.azisababot.crawler

import com.cronutils.model.Cron
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import kotlinx.coroutines.*
import net.azisaba.azisababot.client.MinecraftClient
import net.azisaba.azisababot.server.Server
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.*

class CrawlerImpl(
    override val client: MinecraftClient
) : Crawler {
    private val cronParser: CronParser = CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX))

    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(4)

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val scheduledTasks: MutableMap<String, ScheduledFuture<*>> = ConcurrentHashMap()

    override fun schedule(cronExpression: String) {
        try {
            val cron = cronParser.parse(cronExpression).also { it.validate() }
            val execTime = ExecutionTime.forCron(cron)
            scheduleNext(cronExpression, cron, execTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun unschedule(cronExpression: String) {
        scheduledTasks.remove(cronExpression)?.cancel(false)
    }

    private suspend fun crawl() {
        val servers = Server.servers()
        coroutineScope {
            servers.map { server ->
                async {
                    for (endpoint in server.endpoints) {
                        val status = client.fetchServerStatus(endpoint)
                        val ping = client.fetchPing(endpoint)
                        if (status != null && ping != null) {
                            break
                        }
                    }
                }
            }.awaitAll()
        }
    }

    private fun scheduleNext(expr: String, cron: Cron, execTime: ExecutionTime) {
        val now = ZonedDateTime.now()
        val next = execTime.nextExecution(now).orElse(null)
        if (next != null) {
            val delay = Duration.between(now, next).toMillis()
            val future = scheduler.schedule({
                scope.launch {
                    crawl()
                }
                scheduleNext(expr, cron, execTime)
            }, delay, TimeUnit.MILLISECONDS)
            scheduledTasks[expr] = future
        }
    }
}