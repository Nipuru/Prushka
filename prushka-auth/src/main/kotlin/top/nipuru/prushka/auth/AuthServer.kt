package top.nipuru.prushka.auth

import com.alipay.remoting.ConnectionEventType
import com.alipay.remoting.LifeCycleException
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClient
import net.afyer.afybroker.client.BrokerClientBuilder
import net.afyer.afybroker.core.util.BoltUtils
import org.slf4j.event.Level
import top.nipuru.prushka.auth.config.Config
import top.nipuru.prushka.auth.config.loadConfig
import top.nipuru.prushka.auth.database.DatabaseFactory
import top.nipuru.prushka.auth.http.rootRouting
import top.nipuru.prushka.auth.logger.logger
import top.nipuru.prushka.auth.processor.PlayerRequestHandler
import top.nipuru.prushka.auth.processor.connection.CloseEventAuthProcessor
import top.nipuru.prushka.auth.util.JWTUtil
import top.nipuru.prushka.auth.util.overdue
import top.nipuru.prushka.common.ClientType
import top.nipuru.prushka.common.processor.RequestDispatcher

object AuthServer {
    private lateinit var brokerClient: BrokerClient
    private lateinit var httpServer: EmbeddedServer<*, *>

    private fun buildBrokerClient(builder: BrokerClientBuilder) {
        val dispatcher = RequestDispatcher()

        dispatcher.registerHandler(PlayerRequestHandler())

        builder.registerUserProcessor(dispatcher)
        builder.addConnectionEventProcessor(ConnectionEventType.CLOSE, CloseEventAuthProcessor())
    }

    private fun initHttpServer() {
        httpServer = embeddedServer(Netty, 11300) {
            install(ContentNegotiation) {
                json(Json { prettyPrint = true })
            }
            install(CallLogging) {
                level = Level.INFO
                format { call ->
                    "receive request: ${call.request.httpMethod.value} ${call.request.uri} " +
                            "parameters: ${call.parameters}"
                }
                logger = top.nipuru.prushka.auth.logger.logger
            }
            install(Authentication) {
                jwt {
                    verifier(JWTUtil.makeVerifier())
                    validate { credentials ->
                        JWTPrincipal(credentials.payload)
                    }
                    challenge { _, _ ->
                        call.overdue()
                    }
                }
            }
            routing {
                rootRouting()
            }
        }
        httpServer.start(wait = false)
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

    fun startup() {
        val config = loadConfig()

        initDataSource(config)
        initBrokerClient(config)
        initHttpServer()
    }

    fun shutdown() {
        DatabaseFactory.shutdown()
        brokerClient.shutdown()
        httpServer.stop()
    }
}

fun main() {
    AuthServer.startup()
    Runtime.getRuntime().addShutdownHook(Thread { AuthServer.shutdown() })
    Thread.currentThread().join()
}









