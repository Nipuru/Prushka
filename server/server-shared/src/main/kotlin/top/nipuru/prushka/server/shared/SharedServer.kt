package top.nipuru.prushka.server.shared

import com.alipay.remoting.ConnectionEventType
import com.alipay.remoting.LifeCycleException
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClient
import net.afyer.afybroker.client.BrokerClientBuilder
import net.afyer.afybroker.core.util.BoltUtils
import top.nipuru.prushka.server.common.ClientType
import top.nipuru.prushka.server.common.processor.RequestDispatcher
import top.nipuru.prushka.server.shared.SharedServer.shutdown
import top.nipuru.prushka.server.shared.SharedServer.startup
import top.nipuru.prushka.server.shared.config.Config
import top.nipuru.prushka.server.shared.config.loadConfig
import top.nipuru.prushka.server.shared.database.DatabaseFactory
import top.nipuru.prushka.server.shared.logger.logger
import top.nipuru.prushka.server.shared.service.PlayerInfoService
import top.nipuru.prushka.server.shared.processor.GetPlayerInfoHandler
import top.nipuru.prushka.server.shared.processor.GetPlayerInfosHandler
import top.nipuru.prushka.server.shared.processor.GetTimeSharedProcessor
import top.nipuru.prushka.server.shared.processor.PlayerInfoUpdateHandler
import top.nipuru.prushka.server.shared.processor.connection.CloseEventSharedProcessor
import top.nipuru.prushka.server.shared.processor.connection.ConnectEventSharedProcessor

fun main() {
    startup()
    Runtime.getRuntime().addShutdownHook(Thread { shutdown() })
}

internal object SharedServer {
    private lateinit var brokerClient: BrokerClient

    fun startup() {
        val config = loadConfig()

        initDataSource(config)
        initBrokerClient(config)
    }

    fun shutdown() {
        brokerClient.shutdown()
        DatabaseFactory.shutdown()
    }

    private fun buildBrokerClient(builder: BrokerClientBuilder) {
        builder.addConnectionEventProcessor(ConnectionEventType.CONNECT, ConnectEventSharedProcessor())
        builder.addConnectionEventProcessor(ConnectionEventType.CLOSE, CloseEventSharedProcessor())

        val dispatcher = RequestDispatcher()
        dispatcher.registerHandler(top.nipuru.prushka.server.shared.processor.GetPlayerInfoHandler())
        dispatcher.registerHandler(top.nipuru.prushka.server.shared.processor.GetPlayerInfosHandler())
        dispatcher.registerHandler(top.nipuru.prushka.server.shared.processor.PlayerInfoUpdateHandler())
        builder.registerUserProcessor(dispatcher)

        builder.registerUserProcessor(GetTimeSharedProcessor())
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
            builder.name(top.nipuru.prushka.server.common.ClientType.SHARED)
            builder.type(top.nipuru.prushka.server.common.ClientType.SHARED)
            this.buildBrokerClient(builder)

            brokerClient = builder.build()
            Broker.setClient(brokerClient)
            BoltUtils.initProtocols()
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
