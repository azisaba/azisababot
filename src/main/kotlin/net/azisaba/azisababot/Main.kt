package net.azisaba.azisababot

import com.charleskorn.kaml.Yaml
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.azisaba.azisababot.config.Config
import org.jetbrains.exposed.v1.jdbc.Database
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

val config: Config = config()

val dataSource: HikariDataSource = dataSource()

fun main() {
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