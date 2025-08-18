package server.bukkit.gameplay.core

import net.afyer.afybroker.client.Broker
import server.bukkit.constant.REWARD_PROPERTY
import server.bukkit.constant.PROPERTY_COIN
import server.bukkit.constant.PROPERTY_POINTS
import server.bukkit.gameplay.player.*
import server.bukkit.logger.LogServer
import server.common.logger.logger
import server.bukkit.time.TimeManager
import server.bukkit.util.submit
import server.common.message.PlayerInfoMessage
import server.common.service.PlayerInfoService

class CoreManager(player: GamePlayer) : BaseManager(player) {
    private lateinit var playerData: PlayerData
    private var playedTimeUpdateTime = 0L   // 上次游戏时间更新的系统时间（ms）
    private var idleTime = 0               // 挂机时间（tick）
    var updateShared = false        // 更新个人信息至公共服务器标记

    fun preload(request: TableInfos) {
        request.preload<PlayerData>()
    }

    fun unpack(dataInfo: DataInfo) {
        playerData = dataInfo.unpack<PlayerData>() ?: PlayerData().also {
            it.logoutTime = TimeManager.now
            it.createTime = TimeManager.now
            player.insert(it)
            updateShared = true
        }
    }

    fun pack(dataInfo: DataInfo) {
        dataInfo.pack(playerData)
    }

    // 更新玩家在线时间
    fun tick(systemTimeMills: Long) {
        updatePlayedTime(systemTimeMills, false)
        updatePublic()
        updateAfk()
    }

    fun onQuit() {
        afk = false
        updatePlayedTime(System.currentTimeMillis(), true)
    }

    /** 货币  */
    var coin: Long
        get() = playerData.coin
        private set(coin) {         // 通过 addCoin subtractCoin 设置
            playerData.coin = coin
            player.update(playerData, PlayerData::coin)
            updateShared = true
        }

    /** 点券  */
    var points: Long
        get() = playerData.points
        set(points) {         // 通过 addPoints subtractPoints 设置
            if (coin == playerData.points) return
            playerData.points = points
            player.update(playerData, PlayerData::points)
        }

    /** 头衔id  */
    var rankId
        get() = playerData.rankId
        set(rankId) {
            playerData.rankId = rankId
            player.update(playerData, PlayerData::rankId)
            updateShared = true
        }

    /** 创建时间  */
    val createTime: Long
        get() = playerData.createTime

    /** 最后离线时间  */
    var logoutTime: Long
        get() = playerData.logoutTime
        private set(time) {
            playerData.logoutTime = time
            player.update(playerData, PlayerData::logoutTime)
            updateShared = true
        }

    /** 重置时间  */
    var resetTime: Long
        get() = playerData.resetTime
        set(time) {
            playerData.resetTime = time
            player.update(playerData, PlayerData::resetTime)
        }

    /** 累计在线时间  */
    var playedTime: Long
        get() = playerData.playedTime
        set(time) {
            playerData.playedTime = time
            player.update(playerData, PlayerData::playedTime)
            updateShared = true
        }

    /** 生日 birthday[0]:月,birthday[1]:日  */
    var birthday: IntArray
        get() = playerData.birthday
        set(birthday) {
            playerData.birthday = birthday
            player.update(playerData, PlayerData::birthday)
            updateShared = true
        }

    /** 用于传输 或者显示给其他玩家  */
    val playerInfo: PlayerInfoMessage
        get() = PlayerInfoMessage().also {
            it.playerId = player.playerId
            it.name = player.name
            it.dbId = player.dbId
            it.coin = playerData.coin
            it.rankId = playerData.rankId
            it.createTime = playerData.createTime
            it.logoutTime = playerData.logoutTime
            it.playedTime = playerData.playedTime
            it.texture = player.skin.texture
        }

    /** 是否在线 */
    var isOnline: Boolean
        get() = playerData.logoutTime == 0L
        set(isOnline) {
            if (isOnline) {
                logoutTime = 0L
            } else {
                logoutTime = TimeManager.now
            }
        }

    var afk = false
        set(afk) {
            idleTime = 0
            if (afk == field) {
                return
            }
            field = afk
            if (field) player.enterAfk()
            else player.exitAfk()
        }

    fun subtractCoin(amount: Long, way: Int): Boolean {
        if (amount == 0L) return true
        if (amount < 0L) {
            logger.error("subtract invalid coin amount: {}", amount)
            return false
        }
        coin -= amount
        LogServer.logSubtractReward(player.playerId, REWARD_PROPERTY, PROPERTY_COIN, amount, way)
        return true
    }

    fun addCoin(amount: Long, way: Int): Boolean {
        if (amount == 0L) return true
        if (amount < 0L) {
            logger.error("add invalid coin amount: {}", amount)
            return false
        }
        coin += amount
        LogServer.logAddReward(player.playerId, REWARD_PROPERTY, PROPERTY_COIN, amount, way)
        return true
    }

    fun subtractPoints(amount: Long, way: Int): Boolean {
        if (amount == 0L) return true
        if (amount < 0L) {
            logger.error("subtract invalid points amount: {}", amount)
            return false
        }
        points -= amount
        LogServer.logSubtractReward(player.playerId, REWARD_PROPERTY, PROPERTY_POINTS, amount, way)
        return true
    }

    fun addPoints(amount: Long, way: Int): Boolean {
        if (amount == 0L) return true
        if (amount < 0L) {
            logger.error("add invalid coin points: {}", amount)
            return false
        }
        points += amount
        LogServer.logAddReward(player.playerId, REWARD_PROPERTY, PROPERTY_POINTS, amount, way)
        return true
    }

    private fun updatePlayedTime(systemTimeMills: Long, force: Boolean) {
        if (!isOnline) return  // 不在线直接退出

        val updateTime = playedTimeUpdateTime
        var playedTime = playerData.playedTime
        if (updateTime == 0L) {
            playedTimeUpdateTime = systemTimeMills
            return
        }
        val delay = (60 * 1000).toLong() // 满一分钟执行一次
        if (!force && (systemTimeMills - updateTime + playedTime) / delay == playedTime / delay) return
        playedTimeUpdateTime = systemTimeMills
        playedTime += systemTimeMills - updateTime
        this.playedTime = playedTime
    }

    // 更新玩家的公共玩家信息
    private fun updatePublic() {
        if (!updateShared) return
        updateShared = false
        val playerInfoService = Broker.getService(PlayerInfoService::class.java)
        submit {
            playerInfoService.insertOrUpdate(playerInfo)
        }
    }

    private fun updateAfk() {
        if (!isOnline) return
        if (afk) return
        idleTime++
        if (idleTime > 5 * 60 * 20) {
            afk = true
        }
    }
}
