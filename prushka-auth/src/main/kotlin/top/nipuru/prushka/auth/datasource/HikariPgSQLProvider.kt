package top.nipuru.prushka.auth.datasource

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

class HikariPgSQLProvider : DataSourceProvider {

    private lateinit var hikari: HikariDataSource

    override fun init(host: String, port: Int, database: String, username: String, password: String) {
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
    }

    override fun shutdown() {
        hikari.close()
    }

    override val dataSource: DataSource
        get() = hikari
}
