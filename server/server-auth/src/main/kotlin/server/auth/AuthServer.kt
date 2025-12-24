package server.auth

import com.alipay.remoting.LifeCycleException
import com.google.gson.FieldNamingPolicy
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.*
import io.ktor.server.routing.*
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClient
import net.afyer.afybroker.client.BrokerClientBuilder
import net.afyer.afybroker.core.util.BoltUtils
import org.slf4j.event.Level
import server.auth.config.Config
import server.auth.http.configureRouting
import server.auth.processor.connection.CloseEventAuthProcessor
import server.auth.service.PlayerServiceImpl
import server.auth.service.SheetServiceImpl
import server.auth.util.JWTUtil
import server.auth.util.overdue
import server.common.ClientType
import server.common.logger.Logger
import server.common.service.PlayerService
import server.common.service.SheetService
import server.common.util.database.DatabaseFactory

object AuthServer {
    private lateinit var brokerClient: BrokerClient
    private lateinit var httpServer: EmbeddedServer<*, *>

    private fun buildBrokerClient(builder: BrokerClientBuilder) {
        builder.registerService(PlayerService::class.java, PlayerServiceImpl())
        builder.registerService(SheetService::class.java, SheetServiceImpl)
        builder.addConnectionEventProcessor(CloseEventAuthProcessor())
    }

    private fun initHttpServer() {
        httpServer = embeddedServer(Netty, 11300) {
            install(ContentNegotiation) {
                gson {
                    setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    setPrettyPrinting()
                }
            }
            install(CORS) {
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Post)
                allowMethod(HttpMethod.Put)
                allowMethod(HttpMethod.Delete)
                allowMethod(HttpMethod.Options)
                allowHeader(HttpHeaders.Authorization)
                allowHeader(HttpHeaders.ContentType)
                anyHost()
            }
            install(CallLogging) {
                level = Level.INFO
                format { call ->
                    "receive request: ${call.request.httpMethod.value} ${call.request.uri} " +
                            "parameters: ${call.parameters}"
                }
                logger = Logger
            }
            install(Authentication) {
                jwt {
                    authHeader(JWTUtil::parseAuthHeader)
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
                configureRouting()
            }
        }
        httpServer.start(wait = false)
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
            builder.name(ClientType.AUTH)
            builder.type(ClientType.AUTH)
            buildBrokerClient(builder)

            brokerClient = builder.build()
            Broker.setClient(builder.build())
            BoltUtils.initProtocols()
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

    fun startup() {
        val config = Config.load()

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









