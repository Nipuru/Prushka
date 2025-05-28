package top.nipuru.prushka.server.database

import com.alipay.remoting.ConnectionEventType
import com.alipay.remoting.LifeCycleException
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClient
import net.afyer.afybroker.client.BrokerClientBuilder
import top.nipuru.prushka.server.common.ClientType
import top.nipuru.prushka.server.common.processor.RequestDispatcher
import top.nipuru.prushka.server.database.DatabaseServer.shutdown
import top.nipuru.prushka.server.database.DatabaseServer.startup
import top.nipuru.prushka.server.database.config.Config
import top.nipuru.prushka.server.database.config.loadConfig
import top.nipuru.prushka.server.database.database.DatabaseFactory
import top.nipuru.prushka.server.database.logger.logger
import top.nipuru.prushka.server.database.service.OfflineDataService
import top.nipuru.prushka.server.database.processor.*
import top.nipuru.prushka.server.database.processor.connection.CloseEventDBProcessor


fun main() {
    startup()
    Runtime.getRuntime().addShutdownHook(Thread { shutdown() })
}

internal object DatabaseServer {
    private lateinit var brokerClient: BrokerClient

    fun startup() {
        val config = loadConfig()

        top.nipuru.prushka.server.database.DatabaseServer.initDataSource(config)
        top.nipuru.prushka.server.database.DatabaseServer.initBrokerClient(config)
    }

    fun shutdown() {
        DatabaseFactory.shutdown()
        top.nipuru.prushka.server.database.DatabaseServer.brokerClient.shutdown()
    }

    private fun buildBrokerClient(builder: BrokerClientBuilder) {
        val dispatcher = RequestDispatcher()
        dispatcher.registerHandler(SaveFileHandler())
        dispatcher.registerHandler(LoadFileHandler())
        dispatcher.registerHandler(PlayerDataRequestHandler())
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
            builder.name(String.format("%s-%d", top.nipuru.prushka.server.common.ClientType.DB, config.dbId))
            builder.type(top.nipuru.prushka.server.common.ClientType.DB)
            top.nipuru.prushka.server.database.DatabaseServer.buildBrokerClient(builder)

            top.nipuru.prushka.server.database.DatabaseServer.brokerClient = builder.build()
            Broker.setClient(top.nipuru.prushka.server.database.DatabaseServer.brokerClient)
            top.nipuru.prushka.server.database.DatabaseServer.brokerClient.startup()
            top.nipuru.prushka.server.database.DatabaseServer.brokerClient.ping()
        } catch (e: LifeCycleException) {
            logger.error("Broker client startup failed!")
            throw e
        } catch (e: Exception) {
            logger.error("Ping to the broker server failed!")
            throw e
        }
    }
}
