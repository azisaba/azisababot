package net.azisaba.azisababot

import com.charleskorn.kaml.Yaml
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.azisaba.azisababot.app.app
import net.azisaba.azisababot.client.MinecraftClient
import net.azisaba.azisababot.config.Config
import net.azisaba.azisababot.crawler.schedule.CrawlSchedule
import net.azisaba.azisababot.crawler.schedule.CrawlScheduleTable
import net.azisaba.azisababot.server.group.ServerGroupTable
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.server.ServerTable
import net.azisaba.azisababot.server.group.ServerGroup
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.ResourceBundle

val config: Config = config()

val bundle: ResourceBundle = ResourceBundle.getBundle("Bundle", config.locale)

val logger: Logger = LoggerFactory.getLogger("Azisababot")

val dataSource: HikariDataSource = dataSource()

val cronParser: CronParser = CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX))

val minecraft: MinecraftClient = MinecraftClient.client(config.client.protocolVersion, config.client.timeout)

suspend fun main() {
    transaction {
        SchemaUtils.create(CrawlScheduleTable)
        CrawlScheduleTable.selectAll().forEach { row ->
            CrawlSchedule.load(row)
        }

        SchemaUtils.create(ServerTable)
        ServerTable.selectAll().forEach { row ->
            Server.load(row)
        }

        SchemaUtils.create(ServerGroupTable)
        ServerGroupTable.selectAll().forEach { row ->
            ServerGroup.load(row)
        }
    }

    app()

    dataSource.close()
}

private fun config(): Config {
    val fileName = "config.yml"
    val configFile = File(fileName)

    if (!configFile.exists()) {
        val resourceStream = object {}.javaClass.getResourceAsStream("/$fileName") ?: error("Could not find $fileName in resources")
        Files.copy(resourceStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    val content = configFile.readText()
    return Yaml.default.decodeFromString(Config.serializer(), content)
}

private fun dataSource(): HikariDataSource {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = config.database.url
        username = System.getenv(config.database.usernameEnv)
        password = System.getenv(config.database.passwordEnv)
        driverClassName = "org.mariadb.jdbc.Driver"
        maximumPoolSize = config.database.maxPoolSize
    }
    return HikariDataSource(hikariConfig).also {
        Database.connect(it)
    }
}