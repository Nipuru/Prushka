package server.bukkit.gameplay.player

import net.afyer.afybroker.client.Broker
import org.bukkit.Bukkit
import server.bukkit.MessageType
import server.bukkit.constant.DAY
import server.bukkit.gameplay.chat.ChatManager
import server.bukkit.gameplay.friend.FriendManager
import server.bukkit.gameplay.inventory.InventoryManager
import server.bukkit.gameplay.reward.RewardManager
import server.bukkit.gameplay.offline.OfflineManager
import server.bukkit.gameplay.skin.SkinManager
import server.bukkit.gameplay.teleport.TeleportManager
import server.bukkit.logger.LogServer
import server.common.logger.Logger
import server.bukkit.nms.hasDisconnected
import server.bukkit.time.TimeManager
import server.common.service.PlayerDataService
import java.util.*
import java.util.regex.Pattern

/**
 * 表示一个玩家，所有 api 都应该由主线程去调用，异步要考虑线程安全问题
 */
class GamePlayer(
    val playerId: Int,
    val dbId: Int,
    val name: String,
    val uniqueId: UUID
) {
    val namePattern: Pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE)
    val dataService: PlayerDataService = Broker.getService(PlayerDataService::class.java, dbId.toString())
    val bukkitPlayer by lazy { Bukkit.getPlayer(uniqueId)!! }

    val writer = DataWriter(this)
    val offline = OfflineManager(this)
    val core = server.bukkit.gameplay.core.CoreManager(this)
    val inventory = InventoryManager(this)
    val friend = FriendManager(this)
    val chat = ChatManager(this)
    val item = RewardManager(this)
    val skin = SkinManager(this)
    val teleport = TeleportManager(this)

    /**
     * 预加载数据 (告诉 dbserver 要加载哪些数据)
     */
    fun preload(request: TableInfos) {
        offline.preload(request)
        core.preload(request)
        inventory.preload(request)
        friend.preload(request)
        chat.preload(request)
        item.preload(request)
        skin.preload(request)
        teleport.preload(request)
    }

    /**
     * 数据解包
     */
    fun unpack(dataInfo: DataInfo) {
        offline.unpack(dataInfo)
        core.unpack(dataInfo)
        inventory.unpack(dataInfo)
        friend.unpack(dataInfo)
        chat.unpack(dataInfo)
        item.unpack(dataInfo)
        skin.unpack(dataInfo)
        teleport.unpack(dataInfo)
    }

    /**
     * 数据装包
     */
    fun pack(dataInfo: DataInfo) {
        offline.pack(dataInfo)
        core.pack(dataInfo)
        inventory.pack(dataInfo)
        friend.pack(dataInfo)
        chat.pack(dataInfo)
        item.pack(dataInfo)
        skin.pack(dataInfo)
        teleport.pack(dataInfo)
    }

    fun init() {
        Logger.info("Init GamePlayer: {}", name)
        friend.init()
    }

    /**
     * 新玩家初始化
     */
    fun initNew() {
        Logger.info("Init new GamePlayer: {}", name)

        LogServer.logRegister(playerId)
    }

    /**
     * 玩家登录执行
     */
    fun onLogin() {
        Logger.info("GamePlayer: {} has logged in.", name)
        core.isOnline = true
        if (core.resetTime < TimeManager.dayZero) {
            var lastResetTime: Long = core.resetTime
            // 代表是新玩家
            if (lastResetTime == 0L) {
                initNew()
                onNewDay(TimeManager.dayZero)
            } else {
                // 最多重置30天
                if (TimeManager.dayZero - lastResetTime > 30 * DAY) {
                    lastResetTime = TimeManager.dayZero - 30 * DAY
                }
                while (lastResetTime + DAY <= TimeManager.dayZero) {
                    lastResetTime += DAY
                    onNewDay(lastResetTime)
                }
            }
        }
        skin.applySkin()
        LogServer.logLogin(playerId, bukkitPlayer.address.address.hostAddress)
    }

    /**
     * 玩家离线执行
     */
    fun onLogout() {
        Logger.info("GamePlayer: {} has logged out.", name)

        LogServer.logLogout(playerId, bukkitPlayer.address.address.hostAddress)
    }

    /**
     * 玩家加入服务器执行
     */
    fun onJoin() {
        Logger.info("GamePlayer: {} has joined", name)

        // 玩家登录
        if (!core.isOnline) {
            onLogin()
        }

        teleport.onJoin()
        inventory.onJoin()
        offline.onJoin()
    }

    /**
     * 玩家退出服务器执行
     */
    fun onQuit() {
        Logger.info("GamePlayer: {} has quit.", name)
        teleport.onQuit()
        inventory.onQuit()
        core.onQuit()

        // 判断玩家是否断开连接（跨服不会断开连接 因为是被强制移出玩家列表 并没有断连）
        if (bukkitPlayer.hasDisconnected()) {
            core.isOnline = false
            onLogout()
        }

        // 最后再调用一次 tick 方法
        tick(System.currentTimeMillis())
    }

    fun enterAfk() {
        Logger.info("GamePlayer: {} enter afk.", name)
        MessageType.INFO.sendMessage(bukkitPlayer, "你进入了挂机模式")
    }

    fun exitAfk() {
        Logger.info("GamePlayer: {} quit afk.", name)
        MessageType.INFO.sendMessage(bukkitPlayer, "你离开了挂机模式")
    }

    /**
     * 每 server tick 执行，频率不是固定的
     * 玩家一定在线，玩家退出时会调用一次
     */
    fun tick(systemTimeMillis: Long) {
        core.tick(systemTimeMillis)

        // 最后执行
        offline.tick()
        writer.write()
    }

    /**
     * 现实中的新的一天，时间必定为 0 时, 会执行一次。
     * 并且会补不在线的天数，最多 30 天
     */
    fun onNewDay(time: Long) {
        core.resetTime = time
    }


}
