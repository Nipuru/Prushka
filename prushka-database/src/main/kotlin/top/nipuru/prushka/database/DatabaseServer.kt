package top.nipuru.prushka.database

import com.alipay.remoting.ConnectionEventType
import com.alipay.remoting.LifeCycleException
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClient
import net.afyer.afybroker.client.BrokerClientBuilder
import org.jetbrains.exposed.sql.Database
import top.nipuru.prushka.common.ClientType
import top.nipuru.prushka.common.processor.RequestDispatcher
import top.nipuru.prushka.database.DatabaseServer.shutdown
import top.nipuru.prushka.database.DatabaseServer.startup
import top.nipuru.prushka.database.config.Config
import top.nipuru.prushka.database.config.loadConfig
import top.nipuru.prushka.database.datasource.DataSourceProvider
import top.nipuru.prushka.database.datasource.HikariPgSQLProvider
import top.nipuru.prushka.database.logger.logger
import top.nipuru.prushka.database.offline.OfflineDataManager
import top.nipuru.prushka.database.processor.*
import top.nipuru.prushka.database.processor.connection.CloseEventDBProcessor


fun main() {
    startup()
    Runtime.getRuntime().addShutdownHook(Thread { shutdown() })
}

internal object DatabaseServer {
    private lateinit var brokerClient: BrokerClient
    private lateinit var dataSourceProvider: DataSourceProvider

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
        dispatcher.registerHandler(SaveFileHandler())
        dispatcher.registerHandler(LoadFileHandler())
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
        Database.connect(dataSourceProvider.dataSource)
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
