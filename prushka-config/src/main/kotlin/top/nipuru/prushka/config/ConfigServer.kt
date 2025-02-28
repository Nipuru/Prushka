package top.nipuru.prushka.config

import com.alipay.remoting.ConnectionEventType
import com.alipay.remoting.LifeCycleException
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClient
import net.afyer.afybroker.client.BrokerClientBuilder
import top.nipuru.prushka.common.ClientType
import top.nipuru.prushka.common.processor.RequestDispatcher
import top.nipuru.prushka.config.config.Config
import top.nipuru.prushka.config.config.loadConfig
import top.nipuru.prushka.config.logger.logger
import top.nipuru.prushka.config.processor.ConfigMessageHandler
import top.nipuru.prushka.config.processor.ConfigMessageTypeHandler
import top.nipuru.prushka.config.processor.connection.CloseEventDBProcessor
import top.nipuru.prushka.config.reader.FileReader


fun main() {
    ConfigServer.startup()
    Runtime.getRuntime().addShutdownHook(Thread { ConfigServer.shutdown() })
    startConsoleCommandListener()
}

private fun startConsoleCommandListener() {
    Thread {
        val inputReader = System.`in`.bufferedReader()
        while (true) {
            // 读取用户输入
            val command = inputReader.readLine()?.trim() ?: break

            when (command) {
                "/reload" -> {
                    println("触发配置重载...")
                    ConfigServer.reload()
                    println("配置重载完成")
                    val size = FileReader.files.size
                    println("当前文件数量: $size")
                }

                else -> println("未知命令: $command")
            }
        }
    }.apply {
        isDaemon = true // 设置为守护线程，主线程退出时自动结束
        start()
    }
}

internal object ConfigServer {
    private lateinit var brokerClient: BrokerClient

    fun startup() {
        val config = loadConfig()
        FileReader.init(config)
        initBrokerClient(config)
    }

    fun reload() {
        val config = loadConfig()
        FileReader.reload(config)
    }

    fun shutdown() {
        brokerClient.shutdown()
    }

    private fun buildBrokerClient(builder: BrokerClientBuilder) {
        val dispatcher = RequestDispatcher()
        dispatcher.registerHandler(ConfigMessageHandler())
        dispatcher.registerHandler(ConfigMessageTypeHandler())
        builder.registerUserProcessor(dispatcher)
        builder.addConnectionEventProcessor(ConnectionEventType.CLOSE, CloseEventDBProcessor())
    }

    private fun initBrokerClient(config: Config) {
        try {
            val builder = BrokerClient.newBuilder()
            builder.host(config.broker!!.host!!)
            builder.port(config.broker!!.port!!)
            builder.name(String.format("%s", ClientType.LOG))
            builder.type(ClientType.LOG)
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
