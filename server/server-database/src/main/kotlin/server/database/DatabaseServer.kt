package server.database

import com.alipay.remoting.LifeCycleException
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClient
import net.afyer.afybroker.client.BrokerClientBuilder
import server.common.ClientType
import server.common.logger.Logger
import server.common.service.FileService
import server.common.service.OfflineDataService
import server.common.service.PlayerDataService
import server.common.util.database.DatabaseFactory
import server.database.DatabaseServer.shutdown
import server.database.DatabaseServer.startup
import server.database.config.Config
import server.database.processor.PlayerOfflineDataDBProcessor
import server.database.processor.connection.CloseEventDBProcessor
import server.database.service.FileServiceImpl
import server.database.service.OfflineDataServiceImpl
import server.database.service.PlayerDataServiceImpl


fun main() {
    startup()
    Runtime.getRuntime().addShutdownHook(Thread { shutdown() })
}

internal object DatabaseServer {
    private lateinit var brokerClient: BrokerClient

    fun startup() {
        val config = Config.load()

        initDataSource(config)
        initBrokerClient(config)
    }

    fun shutdown() {
        DatabaseFactory.shutdown()
        brokerClient.shutdown()
    }

    private fun buildBrokerClient(builder: BrokerClientBuilder, config: Config) {
        val dbId = config.dbId.toString()
        builder.registerService(FileService::class.java, FileServiceImpl(), dbId)
        builder.registerService(PlayerDataService::class.java, PlayerDataServiceImpl(), dbId)
        builder.registerService(OfflineDataService::class.java, OfflineDataServiceImpl, dbId)
        builder.registerUserProcessor(PlayerOfflineDataDBProcessor())
        builder.addConnectionEventProcessor(CloseEventDBProcessor())
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
            builder.name(String.format("%s-%d", ClientType.DB, config.dbId))
            builder.type(ClientType.DB)
            buildBrokerClient(builder, config)

            brokerClient = builder.build()
            Broker.setClient(brokerClient)
            brokerClient.startup()
            brokerClient.ping()
            brokerClient.printInformation(Logger)
        } catch (e: LifeCycleException) {
            Logger.error("Broker client startup failed!")
            throw e
        } catch (e: Exception) {
            Logger.error("Ping to the broker server failed!")
            throw e
        }
    }
}
