package server.bukkit

import com.alipay.remoting.rpc.protocol.UserProcessor
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.core.util.ConnectionEventTypeProcessor
import net.kyori.adventure.key.Key
import org.bukkit.Location
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import server.bukkit.command.*
import server.bukkit.gameplay.player.GamePlayerManager
import server.bukkit.listener.*
import server.bukkit.nms.getServerThreadExecutor
import server.bukkit.processor.*
import server.bukkit.processor.connection.CloseEventBukkitProcessor
import server.bukkit.processor.connection.ConnectEventBukkitProcessor
import server.bukkit.scheduler.AudienceMessenger
import server.bukkit.scheduler.ServerTickTask
import server.bukkit.time.TimeManager
import server.bukkit.util.CommandTree
import server.bukkit.util.ScheduleTask
import server.bukkit.util.register
import server.bukkit.util.text.TextFactory
import server.bukkit.util.text.font.Bitmap
import server.common.ClientTag
import server.common.sheet.Sheet
import server.common.sheet.getAllStBitmap
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Bukkit 插件主类
 *
 * @author Nipuru
 * @since 2024/9/16 0:17
 */
object BukkitPlugin : JavaPlugin() {

    // 公共属性
    val enableLatch = CountDownLatch(1)
    val serverThread = server.getServerThreadExecutor()
    val bizThread = ThreadPoolExecutor(
        0, Int.MAX_VALUE,
        60L, TimeUnit.SECONDS,
        SynchronousQueue(),
        ThreadFactoryBuilder().setDaemon(false).setNameFormat("Prushka-bizThread-%d").build()
    )

    // 私有属性
    private val spawnLocations: Cache<String, Location> = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build()
    private val serverThreadDispatcher = serverThread.asCoroutineDispatcher()
    private val bizThreadDispatcher = bizThread.asCoroutineDispatcher()

    // 公共拓展方法
    val Dispatchers.ServerThread: CoroutineDispatcher get() = serverThreadDispatcher
    val Dispatchers.BizThread: CoroutineDispatcher get() = bizThreadDispatcher

    override fun onLoad() {
        // 注册 broker-client 信息
        Broker.buildAction { builder ->
            builder.addTag(ClientTag.GAME)
            newConnectionProcessors().forEach { builder.addConnectionEventProcessor(it) }
            newProcessors().forEach { builder.registerUserProcessor(it) }
        }
    }

    override fun onEnable() {
        reload()
        TimeManager.init()
        GamePlayerManager.loadAll()
        newScheduleTasks().forEach { it.schedule(this) }
        newListeners().forEach { it.register(this) }
        newCommands().forEach { it.register(this) }
        enableLatch.countDown()
    }

    override fun onDisable() {
        GamePlayerManager.unloadAll()

        // 关闭并等待事务线程池
        bizThread.shutdown()
        bizThread.awaitTermination(1L, TimeUnit.MINUTES)
        TimeManager.cancel()
    }

    fun reload() {
        // 保存并加载 config.yaml
        saveDefaultConfig()
        reloadConfig()
        // 加载配置表
        val serverFolder = File(dataFolder.absolutePath).parentFile.parentFile
        Sheet.load(File(serverFolder.parentFile, "sheet").absolutePath)

        initTextFactory()
    }

    private fun initTextFactory() {
        // 生成 bitmap 字体
        // 这里要确保和 python 工具使用一样的算法 /tool/export_bitmap.py
        val splits = listOf(
            "split_pos_1", "split_pos_2", "split_pos_4", "split_pos_8",
            "split_pos_16", "split_pos_32", "split_pos_64", "split_pos_128",
            "split_neg_1", "split_neg_2", "split_neg_4", "split_neg_8",
            "split_neg_16", "split_neg_32", "split_neg_64", "split_neg_128"
        )
        val bitmaps = mutableListOf<Bitmap>()
        var unicode = 0x1000
        Sheet.getAllStBitmap().values.forEach { cfg ->
            val chars = mutableListOf<String>()
            repeat(cfg.row) {
                val builder = StringBuilder()
                repeat(cfg.column) {
                    builder.append(unicode.toChar())
                    unicode += 1
                }
                chars.add(builder.toString())
            }
            val width = (cfg.imgWidth * cfg.height * cfg.row) / (cfg.imgHeight * cfg.column) +
                    ((cfg.height shr 31) and 1) + 1
            val bitmap = Bitmap(cfg.configId, Key.key("prushka:bitmap"), width, *chars.toTypedArray())
            bitmaps.add(bitmap)
        }
        val resourceResolver = { fileName: String -> getResource("font/$fileName") }
        TextFactory.init(resourceResolver, splits, bitmaps)
    }

    private fun newScheduleTasks(): Sequence<ScheduleTask> = sequenceOf(
        ServerTickTask(),
        AudienceMessenger
    )

    // 在这里添加监听器
    private fun newListeners(): Sequence<Listener> = sequenceOf(
        AsyncPlayerPreLoginListener(),
        PlayerJoinListener(),
        PlayerQuitListener(),
        PlayerChatListener(),
        PlayerCommandListener(),
        PlayerMoveListener(),
        PlayerSpawnLocationListener(spawnLocations.asMap()),
        ServerExceptionListener(),
        UnknownCommandListener()
    )

    // 在这里添加网络处理器
    private fun newConnectionProcessors(): Sequence<ConnectionEventTypeProcessor> = sequenceOf(
        ConnectEventBukkitProcessor(), CloseEventBukkitProcessor()
    )

    private fun newProcessors(): Sequence<UserProcessor<*>> = sequenceOf(
        PlayerDataTransferBukkitProcessor(),
        PlayerOfflineDataBukkitProcessor(),
        PlayerChatServerProcessor(),
        PlayerPrivateChatServerProcessor(),
        DebugTimeGameProcessor(),
        GetPlayerLocationBukkitProcessor(),
        TeleportOrSpawnBukkitProcessor(spawnLocations.asMap()),
        AudienceBukkitProcessor(),
    )

    // 在这里添加命令
    private fun newCommands(): Sequence<CommandTree> = sequenceOf(
        WhereAmICommand(),
        OperatorCommand(),
        AfkCommand(),
        FriendCommand(),
        MsgCommand(),
        MsgModeCommand(),
        IgnoreCommand(),
    )
}


