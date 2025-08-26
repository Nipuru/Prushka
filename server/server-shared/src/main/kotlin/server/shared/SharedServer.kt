package server.shared

import com.alipay.remoting.ConnectionEventType
import com.alipay.remoting.LifeCycleException
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClient
import net.afyer.afybroker.client.BrokerClientBuilder
import net.afyer.afybroker.core.util.BoltUtils
import server.common.logger.Logger
import server.common.service.PlayerInfoService
import server.shared.SharedServer.shutdown
import server.shared.SharedServer.startup
import server.shared.config.Config
import server.shared.database.DatabaseFactory
import server.shared.processor.GetTimeSharedProcessor
import server.shared.processor.connection.CloseEventSharedProcessor
import server.shared.processor.connection.ConnectEventSharedProcessor
import server.shared.service.PlayerInfoServiceImpl

fun main() {
    startup()
    Runtime.getRuntime().addShutdownHook(Thread { shutdown() })
}

internal object SharedServer {
    private lateinit var brokerClient: BrokerClient

    fun startup() {
        val config = Config.load()

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

        builder.registerService(PlayerInfoService::class.java, PlayerInfoServiceImpl())

        builder.registerUserProcessor(GetTimeSharedProcessor())
    }

    private fun initDataSource(config: Config) {
        val datasource = config.datasource
        DatabaseFactory.init(
            datasource.host, datasource.port,
            datasource.database, datasource.username, datasource.password
        )
    }

    private fun initBrokerClient(config: Config) {
        try {
            val builder = BrokerClient.newBuilder()
            builder.host(config.broker.host)
            builder.port(config.broker.port)
            builder.name(server.common.ClientType.SHARED)
            builder.type(server.common.ClientType.SHARED)
            this.buildBrokerClient(builder)

            brokerClient = builder.build()
            Broker.setClient(brokerClient)
            BoltUtils.initProtocols()
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
