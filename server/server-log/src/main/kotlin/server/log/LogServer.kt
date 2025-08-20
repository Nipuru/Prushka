package server.log

import com.alipay.remoting.ConnectionEventType
import com.alipay.remoting.LifeCycleException
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClient
import net.afyer.afybroker.client.BrokerClientBuilder
import server.common.logger.Logger
import server.common.service.LogService
import server.log.LogServer.shutdown
import server.log.LogServer.startup
import server.log.config.Config
import server.log.config.loadConfig
import server.log.database.DatabaseFactory
import server.log.processor.connection.CloseEventDBProcessor
import server.log.service.LogServiceImpl


fun main() {
    startup()
    Runtime.getRuntime().addShutdownHook(Thread { shutdown() })
}

internal object LogServer {
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
        builder.registerService(LogService::class.java, LogServiceImpl())
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
            builder.name(String.format("%s", server.common.ClientType.LOG))
            builder.type(server.common.ClientType.LOG)
            buildBrokerClient(builder)

            brokerClient = builder.build()
            Broker.setClient(brokerClient)
            brokerClient.startup()
            brokerClient.ping()
        } catch (e: LifeCycleException) {
            Logger.error("Broker client startup failed!")
            throw e
        } catch (e: Exception) {
            Logger.error("Ping to the broker server failed!")
            throw e
        }
    }
}
