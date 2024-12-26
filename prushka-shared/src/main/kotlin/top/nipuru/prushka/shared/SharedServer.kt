package top.nipuru.prushka.shared

import com.alipay.remoting.ConnectionEventType
import com.alipay.remoting.LifeCycleException
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClient
import net.afyer.afybroker.client.BrokerClientBuilder
import net.afyer.afybroker.core.util.BoltUtils
import top.nipuru.prushka.common.ClientType
import top.nipuru.prushka.common.processor.RequestDispatcher
import top.nipuru.prushka.shared.SharedServer.shutdown
import top.nipuru.prushka.shared.SharedServer.startup
import top.nipuru.prushka.shared.config.Config
import top.nipuru.prushka.shared.config.loadConfig
import top.nipuru.prushka.shared.database.DatabaseFactory
import top.nipuru.prushka.shared.logger.logger
import top.nipuru.prushka.shared.player.PlayerInfoManager
import top.nipuru.prushka.shared.processor.GetPlayerInfoHandler
import top.nipuru.prushka.shared.processor.GetPlayerInfosHandler
import top.nipuru.prushka.shared.processor.GetTimeSharedProcessor
import top.nipuru.prushka.shared.processor.PlayerInfoUpdateHandler
import top.nipuru.prushka.shared.processor.connection.CloseEventSharedProcessor
import top.nipuru.prushka.shared.processor.connection.ConnectEventSharedProcessor

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

        PlayerInfoManager.init()
    }

    fun shutdown() {
        brokerClient.shutdown()
        DatabaseFactory.shutdown()
    }

    private fun buildBrokerClient(builder: BrokerClientBuilder) {
        builder.addConnectionEventProcessor(ConnectionEventType.CONNECT, ConnectEventSharedProcessor())
        builder.addConnectionEventProcessor(ConnectionEventType.CLOSE, CloseEventSharedProcessor())

        val dispatcher = RequestDispatcher()
        dispatcher.registerHandler(GetPlayerInfoHandler())
        dispatcher.registerHandler(GetPlayerInfosHandler())
        dispatcher.registerHandler(PlayerInfoUpdateHandler())
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
            builder.name(ClientType.SHARED)
            builder.type(ClientType.SHARED)
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
