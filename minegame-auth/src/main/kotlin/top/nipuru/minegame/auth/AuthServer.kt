package top.nipuru.minegame.auth

import com.alipay.remoting.ConnectionEventType
import com.alipay.remoting.LifeCycleException
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClient
import net.afyer.afybroker.client.BrokerClientBuilder
import net.afyer.afybroker.core.util.BoltUtils
import top.nipuru.minegame.auth.AuthServer.dataSourceProvider
import top.nipuru.minegame.auth.AuthServer.shutdown
import top.nipuru.minegame.auth.AuthServer.startup
import top.nipuru.minegame.auth.config.Config
import top.nipuru.minegame.auth.config.loadConfig
import top.nipuru.minegame.auth.datasource.DataSourceProvider
import top.nipuru.minegame.auth.datasource.HikariPgSQLProvider
import top.nipuru.minegame.auth.processor.QueryUserHandler
import top.nipuru.minegame.auth.processor.connection.CloseEventAuthProcessor
import top.nipuru.minegame.auth.user.UserManager
import top.nipuru.minegame.auth.logger.logger
import top.nipuru.minegame.common.ClientType
import top.nipuru.minegame.common.processor.RequestDispatcher
import javax.sql.DataSource

val dataSource: DataSource
    get() = dataSourceProvider.dataSource


fun main() {
    startup()
    Runtime.getRuntime().addShutdownHook(Thread { shutdown() })
}

internal object AuthServer {
    private lateinit var brokerClient: BrokerClient
    lateinit var dataSourceProvider: DataSourceProvider

    fun startup() {
        val config = loadConfig()

        initDataSource(config)
        initBrokerClient(config)

        UserManager.init()
    }

    fun shutdown() {
        dataSourceProvider.shutdown()
        brokerClient.shutdown()
    }

    private fun buildBrokerClient(builder: BrokerClientBuilder) {
        val dispatcher = RequestDispatcher()

        dispatcher.registerHandler(QueryUserHandler())

        builder.registerUserProcessor(dispatcher)
        builder.addConnectionEventProcessor(ConnectionEventType.CLOSE, CloseEventAuthProcessor())
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








