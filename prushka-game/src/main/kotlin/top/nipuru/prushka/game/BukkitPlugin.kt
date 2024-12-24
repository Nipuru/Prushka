package top.nipuru.prushka.game

import com.alipay.remoting.ConnectionEventType
import com.google.common.cache.CacheBuilder
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClientBuilder
import top.nipuru.prushka.common.ClientTag
import top.nipuru.prushka.common.processor.RequestDispatcher
import top.nipuru.prushka.game.command.*
import top.nipuru.prushka.game.gameplay.player.GamePlayer
import top.nipuru.prushka.game.gameplay.player.GamePlayers
import top.nipuru.prushka.game.listener.*
import top.nipuru.prushka.game.processor.*
import top.nipuru.prushka.game.scheduler.ServerTickTask
import top.nipuru.prushka.game.time.TimeManager
import top.nipuru.prushka.game.util.bizThread
import top.nipuru.prushka.game.util.register
import top.nipuru.prushka.game.processor.connection.CloseEventGameProcessor
import top.nipuru.prushka.game.processor.connection.ConnectEventGameProcessor
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * @author Nipuru
 * @since 2024/9/16 0:17
 */
val enableLatch = CountDownLatch(1)

lateinit var plugin: Plugin
    private set

class BukkitPlugin : JavaPlugin() {
    private val pendingPlayers = mutableMapOf<UUID, GamePlayer>()
    private val spawnLocations = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build<String, Location>()

    override fun onLoad() {
        plugin = this
        Broker.buildAction(this::buildBrokerClient)
    }

    private fun buildBrokerClient(builder: BrokerClientBuilder) {
        builder.addConnectionEventProcessor(ConnectionEventType.CONNECT, ConnectEventGameProcessor())
        builder.addConnectionEventProcessor(ConnectionEventType.CLOSE, CloseEventGameProcessor())

        val dispatcher = RequestDispatcher()
        dispatcher.registerHandler(KickPlayerHandler())
        builder.registerUserProcessor(dispatcher)
        builder.addTag(ClientTag.GAME)

        builder.registerUserProcessor(PlayerDataTransferGameProcessor())
        builder.registerUserProcessor(PlayerOfflineDataGameProcessor())
        builder.registerUserProcessor(PlayerChatServerProcessor())
        builder.registerUserProcessor(PlayerPrivateChatServerProcessor())
        builder.registerUserProcessor(DebugTimeGameProcessor())
    }

    override fun onEnable() {
        GamePlayers.loadAll()

        registerTasks()
        registerListeners()
        registerCommands()
        enableLatch.countDown()
    }

    override fun onDisable() {
        GamePlayers.unloadAll()
        bizThread.shutdown()
        bizThread.awaitTermination(1L, TimeUnit.MINUTES)
        TimeManager.cancel()
    }

    private fun registerTasks() {
        ServerTickTask().schedule()
    }

    private fun registerListeners() {
        AsyncPlayerPreLoginListener(pendingPlayers).register(this)
        PlayerJoinListener(pendingPlayers).register(this)
        PlayerQuitListener().register(this)
        PlayerChatListener().register(this)
        PlayerCommandListener().register(this)
        PlayerMoveListener().register(this)
        PlayerSpawnLocationListener(spawnLocations.asMap()).register(this)
    }

    private fun registerCommands() {
        PrushkaCommand().register(this)
        AfkCommand().register(this)
        WhereAmICommand().register(this)
        FriendCommand().register(this)
        TeleportAtCommand().register(this)
        TeleportAtHereCommand().register(this)
        TeleportAcceptCommand().register(this)
        TeleportDenyCommand().register(this)
    }
}
