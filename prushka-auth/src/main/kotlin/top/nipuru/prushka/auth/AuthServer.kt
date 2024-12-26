package top.nipuru.prushka.auth

import com.alipay.remoting.ConnectionEventType
import com.alipay.remoting.LifeCycleException
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClient
import net.afyer.afybroker.client.BrokerClientBuilder
import net.afyer.afybroker.core.util.BoltUtils
import org.jetbrains.exposed.sql.Database
import top.nipuru.prushka.auth.AuthServer.shutdown
import top.nipuru.prushka.auth.AuthServer.startup
import top.nipuru.prushka.auth.config.Config
import top.nipuru.prushka.auth.config.loadConfig
import top.nipuru.prushka.auth.database.DatabaseFactory
import top.nipuru.prushka.auth.processor.QueryUserHandler
import top.nipuru.prushka.auth.processor.connection.CloseEventAuthProcessor
import top.nipuru.prushka.auth.user.UserManager
import top.nipuru.prushka.auth.logger.logger
import top.nipuru.prushka.common.ClientType
import top.nipuru.prushka.common.processor.RequestDispatcher

fun main() {
    startup()
    Runtime.getRuntime().addShutdownHook(Thread { shutdown() })
}

internal object AuthServer {
    private lateinit var brokerClient: BrokerClient

    fun startup() {
        val config = loadConfig()

        initDataSource(config)
        initBrokerClient(config)

        UserManager.init()
    }

    fun shutdown() {
        DatabaseFactory.shutdown()
        brokerClient.shutdown()
    }

    private fun buildBrokerClient(builder: BrokerClientBuilder) {
        val dispatcher = RequestDispatcher()

        dispatcher.registerHandler(QueryUserHandler())

        builder.registerUserProcessor(dispatcher)
        builder.addConnectionEventProcessor(ConnectionEventType.CLOSE, CloseEventAuthProcessor())
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
            builder.name(ClientType.AUTH)
            builder.type(ClientType.AUTH)
            buildBrokerClient(builder)

            brokerClient = builder.build()
            Broker.setClient(builder.build())
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








