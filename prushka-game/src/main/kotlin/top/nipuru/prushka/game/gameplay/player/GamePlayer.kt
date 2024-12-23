package top.nipuru.prushka.game.gameplay.player

import top.nipuru.prushka.common.message.database.QueryPlayerRequest
import top.nipuru.prushka.game.MessageType
import top.nipuru.prushka.game.constants.DAY
import top.nipuru.prushka.game.constants.Items
import top.nipuru.prushka.game.gameplay.chat.ChatManager
import top.nipuru.prushka.game.gameplay.core.CoreManager
import top.nipuru.prushka.game.gameplay.friend.FriendManager
import top.nipuru.prushka.game.gameplay.inventory.InventoryManager
import top.nipuru.prushka.game.gameplay.item.ItemManager
import top.nipuru.prushka.game.gameplay.offline.OfflineManager
import top.nipuru.prushka.game.gameplay.skin.SkinManager
import top.nipuru.prushka.game.logger.logger
import top.nipuru.prushka.game.nms.hasDisconnected
import top.nipuru.prushka.game.time.TimeManager
import org.bukkit.Bukkit
import java.util.*
import java.util.regex.Pattern
import kotlin.reflect.KProperty1

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
    val bukkitPlayer by lazy { Bukkit.getPlayer(uniqueId)!! }

    private val writer = DataWriter(this)
    val offline = OfflineManager(this)
    val core = CoreManager(this)
    val inventory = InventoryManager(this)
    val friend = FriendManager(this)
    val chat = ChatManager(this)
    val item = ItemManager(this)
    val skin = SkinManager(this)

    /**
     * 预加载数据 (告诉 dbserver 要加载哪些数据)
     */
    fun preload(request: QueryPlayerRequest) {
        offline.preload(request)
        core.preload(request)
        inventory.preload(request)
        friend.preload(request)
        chat.preload(request)
        item.preload(request)
        skin.preload(request)
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
    }

    fun init() {
        logger.info("Init GamePlayer: {}", name)
        friend.init()
    }

    /**
     * 新玩家初始化
     */
    fun initNew() {
        logger.info("Init new GamePlayer: {}", name)
    }

    /**
     * 玩家登录执行
     */
    fun onLogin() {
        logger.info("GamePlayer: {} has logged in.", name)
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
    }

    /**
     * 玩家离线执行
     */
    fun onLogout() {
        logger.info("GamePlayer: {} has logged out.", name)
    }

    /**
     * 玩家加入服务器执行
     */
    fun onJoin() {
        logger.info("GamePlayer: {} has joined", name)

        // 玩家登录
        if (!core.isOnline) {
            onLogin()
        }

        inventory.onJoin()
        offline.onJoin()
    }

    /**
     * 玩家退出服务器执行
     */
    fun onQuit() {
        logger.info("GamePlayer: {} has quit.", name)

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
        logger.info("GamePlayer: {} enter afk.", name)
        MessageType.INFO.sendMessage(bukkitPlayer, "你进入了挂机模式")
    }

    fun exitAfk() {
        logger.info("GamePlayer: {} quit afk.", name)
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

    fun giveRewards(rewards: Array<RewardInfo>, way: Int) {
        for (reward in rewards) {
            when (reward.type) {
                Items.ITEM_PROPERTY -> addProperty(reward.id, reward.num.toLong(), way)
                else -> item.addItem(reward.type, reward.id, reward.num.toLong(), way)
            }
        }
    }

    fun getPropertyAmount(id: Int): Long {
        return when (id) {
            Items.PROPERTY_COIN -> core.coin
            Items.PROPERTY_POINTS -> core.points
            else -> item.getItemAmount(Items.ITEM_PROPERTY, id)
        }
    }

    fun addProperty(id: Int, amount: Long, way: Int): Boolean {
        return when (id) {
            Items.PROPERTY_COIN -> core.addCoin(amount, way)
            Items.PROPERTY_POINTS -> core.addPoints(amount, way)
            else -> item.addItem(Items.ITEM_PROPERTY, id, amount, way)
        }
    }

    fun subtractProperty(id: Int, amount: Long, way: Int): Boolean {
        return when (id) {
            Items.PROPERTY_COIN -> core.subtractCoin(amount, way)
            Items.PROPERTY_POINTS -> core.subtractPoints(amount, way)
            else -> item.subtractItem(Items.ITEM_PROPERTY, id, amount, way)
        }
    }

    fun checkProperties(properties: Map<Int, Int>): Boolean {
        return properties.all { (id, needAmount) ->
            needAmount == 0 || needAmount > 0 && getPropertyAmount(id) >= needAmount
        }
    }

    fun subtractProperties(properties: Map<Int, Int>, way: Int) {
        properties.forEach { (id, amount) ->
            subtractProperty(id, amount.toLong(), way)
        }
    }

    fun <T : Any> update(data: T, vararg properties: KProperty1<T, *>) {
        this.writer.add(DataAction(DataActionType.UPDATE, data, DataConvertor.getProperty(data, properties)))
    }

    fun <T: Any> insert(data: T) {
        this.writer.add(DataAction(DataActionType.INSERT, data, null))
    }

    fun <T: Any> delete(data: T) {
        this.writer.add(DataAction(DataActionType.DELETE, data, null))
    }
}
