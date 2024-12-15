package top.nipuru.minegame.database

import com.alipay.remoting.ConnectionEventType
import com.alipay.remoting.LifeCycleException
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClient
import net.afyer.afybroker.client.BrokerClientBuilder
import top.nipuru.minegame.common.ClientType
import top.nipuru.minegame.common.processor.RequestDispatcher
import top.nipuru.minegame.database.DatabaseServer.dataSourceProvider
import top.nipuru.minegame.database.DatabaseServer.shutdown
import top.nipuru.minegame.database.DatabaseServer.startup
import top.nipuru.minegame.database.config.Config
import top.nipuru.minegame.database.config.loadConfig
import top.nipuru.minegame.database.datasource.DataSourceProvider
import top.nipuru.minegame.database.datasource.HikariPgSQLProvider
import top.nipuru.minegame.database.logger.logger
import top.nipuru.minegame.database.offline.OfflineDataManager
import top.nipuru.minegame.database.processor.*
import top.nipuru.minegame.database.processor.connection.CloseEventDBProcessor
import javax.sql.DataSource


val dataSource: DataSource
    get() = dataSourceProvider.dataSource


fun main() {
    startup()
    Runtime.getRuntime().addShutdownHook(Thread { shutdown() })
}

internal object DatabaseServer {
    private lateinit var brokerClient: BrokerClient
    lateinit var dataSourceProvider: DataSourceProvider

    fun startup() {
        val config = loadConfig()

        initDataSource(config)
        initBrokerClient(config)

        OfflineDataManager.init()
    }

    fun shutdown() {
        dataSourceProvider.shutdown()
        brokerClient.shutdown()
    }

    private fun buildBrokerClient(builder: BrokerClientBuilder) {
        val dispatcher = RequestDispatcher()

        dispatcher.registerHandler(QueryPlayerHandler())
        dispatcher.registerHandler(PlayerTransactionHandler())

        builder.registerUserProcessor(dispatcher)
        builder.registerUserProcessor(PlayerOfflineDataDBProcessor())
        builder.addConnectionEventProcessor(ConnectionEventType.CLOSE, CloseEventDBProcessor())
    }

    private fun initDataSource(config: Config) {
        dataSourceProvider = HikariPgSQLProvider()
        val datasource = config.datasource!!
        dataSourceProvider.init(
            datasource.host!!, datasource.port!!,
            datasource.database!!, datasource.username!!, datasource.password!!
        )
    }

    private fun initBrokerClient(config: Config) {
        try {
            val builder = BrokerClient.newBuilder()
            builder.host(config.broker!!.host!!)
            builder.port(config.broker!!.port!!)
            builder.name(String.format("%s-%d", ClientType.DB, config.dbId))
            builder.type(ClientType.DB)
            this.buildBrokerClient(builder)

            brokerClient = builder.build()
            Broker.setClient(brokerClient)
            brokerClient.startup()
            brokerClient.ping()
        } catch (e: LifeCycleException) {
            logger.error("Broker client startup failed!")
            throw e
        } catch (e: Exception) {
            logger.error("Ping to the broker server failed!")
            throw e
        }
    }
}
