package top.nipuru.prushka.server.log

import com.alipay.remoting.ConnectionEventType
import com.alipay.remoting.LifeCycleException
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClient
import net.afyer.afybroker.client.BrokerClientBuilder
import top.nipuru.prushka.server.common.processor.RequestDispatcher
import top.nipuru.prushka.server.log.LogServer.shutdown
import top.nipuru.prushka.server.log.LogServer.startup
import top.nipuru.prushka.server.log.config.Config
import top.nipuru.prushka.server.log.config.loadConfig
import top.nipuru.prushka.server.log.database.DatabaseFactory
import top.nipuru.prushka.server.log.logger.logger
import top.nipuru.prushka.server.log.processor.LogHandler
import top.nipuru.prushka.server.log.processor.connection.CloseEventDBProcessor


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
        val dispatcher = RequestDispatcher()
        dispatcher.registerHandler(LogHandler())
        builder.registerUserProcessor(dispatcher)
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
            builder.name(String.format("%s", top.nipuru.prushka.server.common.ClientType.LOG))
            builder.type(top.nipuru.prushka.server.common.ClientType.LOG)
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
