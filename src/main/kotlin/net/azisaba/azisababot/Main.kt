package net.azisaba.azisababot

import com.charleskorn.kaml.Yaml
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.azisaba.azisababot.config.Config
import net.azisaba.azisababot.server.Server
import net.azisaba.azisababot.server.ServerTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

val config: Config = config()

val dataSource: HikariDataSource = dataSource()

fun main() {
    transaction {
        SchemaUtils.create(ServerTable)
        ServerTable.selectAll().forEach { row ->
            Server.load(row)
        }
    }

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