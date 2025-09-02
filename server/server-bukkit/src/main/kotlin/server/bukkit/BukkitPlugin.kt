@file:Suppress("UnstableApiUsage")

package server.bukkit

import com.google.common.cache.CacheBuilder
import com.google.common.util.concurrent.ThreadFactoryBuilder
import net.afyer.afybroker.client.Broker
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
            .build()
    )

    override fun onLoad() {
        Broker.buildAction { builder ->
            builder.addTag(ClientTag.GAME)
            sequenceOf(ConnectEventBukkitProcessor(), CloseEventBukkitProcessor()).forEach { builder.addConnectionEventProcessor(it) }
            newProcessors().forEach { builder.registerUserProcessor(it) }
        }
    }

    override fun onEnable() {
        reload()
        GamePlayerManager.loadAll()
        newScheduleTasks().forEach { it.schedule(this) }
        newListeners().forEach { it.register(this) }
        newCommands().forEach { it.register(this) }
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
        // 加载配置表
        val serverFolder = File(dataFolder.absolutePath).parentFile.parentFile
        Sheet.load(File(serverFolder.parentFile, "sheet").absolutePath)
    }

    private fun newScheduleTasks() = sequenceOf(
        ServerTickTask()
    )

    // 在这里添加监听器
    private fun newListeners() = sequenceOf(
        AsyncPlayerPreLoginListener(),
        PlayerJoinListener(),
        PlayerQuitListener(),
        PlayerChatListener(),
        PlayerCommandListener(),
        PlayerMoveListener(),
        PlayerSpawnLocationListener(spawnLocations.asMap()),
        ServerExceptionListener()
    )

    // 在这里添加网络处理器
    private fun newProcessors() = sequenceOf(
        PlayerDataTransferBukkitProcessor(),
        PlayerOfflineDataBukkitProcessor(),
        PlayerChatServerProcessor(),
        PlayerPrivateChatServerProcessor(),
        DebugTimeGameProcessor(),
        GetPlayerLocationBukkitProcessor(),
        TeleportOrSpawnBukkitProcessor(spawnLocations.asMap())
    )

    // 在这里添加命令
    private fun newCommands() = sequenceOf(
        WhereAmICommand(),
        PrushkaCommand(),
        AfkCommand(),
        FriendCommand()
    )
}


