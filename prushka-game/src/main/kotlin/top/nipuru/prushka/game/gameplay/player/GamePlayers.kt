package top.nipuru.prushka.game.gameplay.player

import org.bukkit.Bukkit
import top.nipuru.prushka.game.logger.logger
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object GamePlayers {
    private val byUniqueId: MutableMap<UUID, GamePlayer> = ConcurrentHashMap()
    private val byPlayerId: MutableMap<Int, GamePlayer> = ConcurrentHashMap()

    // 插件启用时调用
    fun loadAll() {
        // 加载在线玩家数据
        for (bukkitPlayer in Bukkit.getOnlinePlayers()) {
            val player = DataReader.read(bukkitPlayer.name, bukkitPlayer.uniqueId, bukkitPlayer.address.address)
            register(player)
            player.init()
            player.onJoin()
        }
    }

    // 插件禁用时调用
    fun unloadAll() {
        // 保存在线玩家数据
        for (bukkitPlayer in Bukkit.getOnlinePlayers()) {
            val player = getPlayer(bukkitPlayer.uniqueId)
            player.onQuit()
            removePlayer(player)
        }
    }

    fun tick() {
        val systemTimeMills = System.currentTimeMillis()
        for (player in byUniqueId.values) {
            player.tick(systemTimeMills)
        }
    }

    fun onNewDay(time: Long) {
        for (player in byUniqueId.values) {
            player.onNewDay(time)
        }
    }

    fun register(player: GamePlayer) {
        if (logger.isDebugEnabled) {
            logger.debug("Register GamePlayer: {}", player.name)
        }
        byUniqueId[player.uniqueId] = player
        byPlayerId[player.playerId] = player
    }

    fun getPlayer(uniqueId: UUID): GamePlayer {
        val gamePlayer = byUniqueId[uniqueId]
            ?: throw NullPointerException("Player with uniqueId $uniqueId is not exist")
        return gamePlayer
    }

    fun hasPlayer(uniqueId: UUID): Boolean {
        return byUniqueId.containsKey(uniqueId)
    }

    fun hasPlayer(playerId: Int): Boolean {
        return byPlayerId.containsKey(playerId)
    }

    fun getPlayer(playerId: Int): GamePlayer {
        val gamePlayer = byPlayerId[playerId]
            ?: throw NullPointerException("Player with playerId $playerId is not exist")
        return gamePlayer
    }

    val players: Collection<GamePlayer>
        get() = Collections.unmodifiableCollection(byUniqueId.values)

    fun removePlayer(player: GamePlayer) {
        byUniqueId.remove(player.uniqueId)
        byPlayerId.remove(player.playerId)
        if (logger.isDebugEnabled) {
            logger.debug("Remove GamePlayer: {}", player.name)
        }
    }
}
