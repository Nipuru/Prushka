package server.bukkit

import com.alipay.remoting.ConnectionEventType
import com.google.common.cache.CacheBuilder
import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.client.BrokerClientBuilder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import server.bukkit.command.AfkCommand
import server.bukkit.command.FriendCommand
import server.bukkit.command.PrushkaCommand
import server.bukkit.command.WhereAmICommand
import server.bukkit.gameplay.player.GamePlayerManager
import server.bukkit.listener.*
import server.bukkit.processor.*
import server.bukkit.processor.connection.CloseEventBukkitProcessor
import server.bukkit.processor.connection.ConnectEventBukkitProcessor
import server.bukkit.scheduler.ServerTickTask
import server.bukkit.time.TimeManager
import server.bukkit.util.register
import server.common.ClientTag
import server.common.logger.Logger
import server.common.sheet.Sheet
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Bukkit 插件主类
 *
 * @author Nipuru
 * @since 2024/9/16 0:17
 */
object BukkitPlugin : JavaPlugin() {

    private val spawnLocations = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build<String, Location>()
    val enableLatch = CountDownLatch(1)
    val bizThread: ExecutorService = Executors.newCachedThreadPool(
        ThreadFactoryBuilder()
            .setDaemon(false)
            .setNameFormat("Prushka-bizThread-%d")
            .build())

    override fun onLoad() {
        Broker.buildAction(this::buildBrokerClient)
    }

    private fun buildBrokerClient(builder: BrokerClientBuilder) {
        builder.addConnectionEventProcessor(ConnectionEventType.CONNECT, ConnectEventBukkitProcessor())
        builder.addConnectionEventProcessor(ConnectionEventType.CLOSE,
            CloseEventBukkitProcessor()
        )

        builder.addTag(ClientTag.GAME)
        builder.registerUserProcessor(PlayerDataTransferBukkitProcessor())
        builder.registerUserProcessor(PlayerOfflineDataBukkitProcessor())
        builder.registerUserProcessor(PlayerChatServerProcessor())
        builder.registerUserProcessor(PlayerPrivateChatServerProcessor())
        builder.registerUserProcessor(DebugTimeGameProcessor())
        builder.registerUserProcessor(GetPlayerLocationBukkitProcessor())
        builder.registerUserProcessor(TeleportOrSpawnBukkitProcessor(spawnLocations.asMap()))
    }

    override fun onEnable() {
        reload()
        GamePlayerManager.loadAll()
        registerTasks()
        registerListeners()
        registerCommands()
        enableLatch.countDown()
    }

    override fun onDisable() {
        GamePlayerManager.unloadAll()
        bizThread.shutdown()
        bizThread.awaitTermination(1L, TimeUnit.MINUTES)
        TimeManager.cancel()
    }

    fun reload() {
        saveDefaultConfig()
        reloadConfig()
        Sheet.load(File(dataFolder, "jsons").absolutePath)
    }

    fun submit(async: Boolean = true, block: () -> Unit) {
        val runnable = Runnable {
            try {
                block.invoke()
            } catch (e: Exception) {
                Logger.error(e.message, e)
            }
        }
        val primaryThread = Bukkit.isPrimaryThread()
        if (async && primaryThread) {
            bizThread.submit(runnable)
        } else if (!primaryThread) {
            Bukkit.getScheduler().runTask(BukkitPlugin, runnable)
        } else {
            runnable.run()
        }
    }

    private fun registerTasks() {
        ServerTickTask().schedule()
    }

    private fun registerListeners() {
        AsyncPlayerPreLoginListener().register(this)
        PlayerJoinListener().register(this)
        PlayerQuitListener().register(this)
        PlayerChatListener().register(this)
        PlayerCommandListener().register(this)
        PlayerMoveListener().register(this)
        PlayerSpawnLocationListener(spawnLocations.asMap()).register(this)
        ServerExceptionListener().register(this)
    }

    private fun registerCommands() {
        @Suppress("UnstableApiUsage")
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            val registrar = commands.registrar()
            WhereAmICommand.register(registrar)
            PrushkaCommand.register(registrar)
            AfkCommand.register(registrar)
            FriendCommand.register(registrar)
        }
    }
}


