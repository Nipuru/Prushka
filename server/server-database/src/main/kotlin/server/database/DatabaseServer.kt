package server.database

import com.alipay.remoting.ConnectionEventType
import com.alipay.remoting.LifeCycleException
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClient
import net.afyer.afybroker.client.BrokerClientBuilder
import server.common.processor.RequestDispatcher
import server.database.DatabaseServer.shutdown
import server.database.DatabaseServer.startup
import server.database.config.Config
import server.database.config.loadConfig
import server.database.database.DatabaseFactory
import server.database.logger.logger
import server.database.processor.PlayerDataQueryHandler
import server.database.processor.PlayerDataTransactionHandler
import server.database.processor.PlayerOfflineDataDBProcessor
import server.database.processor.connection.CloseEventDBProcessor


fun main() {
    startup()
    Runtime.getRuntime().addShutdownHook(Thread { shutdown() })
}

internal object DatabaseServer {
    private lateinit var brokerClient: BrokerClient

    fun startup() {
        val config = loadConfig()

        initDataSource(config)
        initBrokerClient(config)
    }

    fun shutdown() {
        DatabaseFactory.shutdown()
        brokerClient.shutdown()
    }

    private fun buildBrokerClient(builder: BrokerClientBuilder) {
        val dispatcher = RequestDispatcher()
        dispatcher.registerHandler(server.database.processor.FileSaveHandler())
        dispatcher.registerHandler(server.database.processor.FileLoadHandler())
        dispatcher.registerHandler(PlayerDataQueryHandler())
        dispatcher.registerHandler(PlayerDataTransactionHandler())

        builder.registerUserProcessor(dispatcher)
        builder.registerUserProcessor(PlayerOfflineDataDBProcessor())
        builder.addConnectionEventProcessor(ConnectionEventType.CLOSE, CloseEventDBProcessor())
    }

    private fun initDataSource(config: Config) {
        val datasource = config.datasource!!
        DatabaseFactory.init(
            datasource.host!!, datasource.port!!,
            datasource.database!!, datasource.username!!, datasource.password!!
        )
    }

    private fun initBrokerClient(config: Config) {
        try {
            val builder = BrokerClient.newBuilder()
            builder.host(config.broker!!.host!!)
            builder.port(config.broker!!.port!!)
            builder.name(String.format("%s-%d", server.common.ClientType.DB, config.dbId))
            builder.type(server.common.ClientType.DB)
            buildBrokerClient(builder)

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
