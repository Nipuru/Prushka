package top.nipuru.prushka.server.database.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database


/**
 * @author Nipuru
 * @since 2024/12/25 17:04
 */
object DatabaseFactory {

    private lateinit var hikari: HikariDataSource

    fun init(host: String, port: Int, database: String, username: String, password: String) {
        val config = HikariConfig()
        config.poolName = "PublicServer-hikari-postgres"

        val jdbcUrl = "jdbc:postgresql://$host:$port/$database"
        config.jdbcUrl = jdbcUrl
        config.driverClassName = "org.postgresql.Driver"
        config.username = username
        config.password = password
        config.maximumPoolSize = 10
        config.minimumIdle = 10
        config.maxLifetime = 1800000
        config.connectionTimeout = 5000
        this.hikari = HikariDataSource(config)

        Database.connect(hikari)
    }

    fun shutdown() {
        hikari.close()
    }
}