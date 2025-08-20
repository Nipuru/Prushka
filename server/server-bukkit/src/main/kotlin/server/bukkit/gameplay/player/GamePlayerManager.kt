package server.bukkit.gameplay.player

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import server.common.logger.Logger
import java.util.*
import java.util.concurrent.ConcurrentHashMap

val Player.gamePlayer: GamePlayer get() = GamePlayerManager.getPlayer(uniqueId)

object GamePlayerManager {
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
        Logger.info("Register GamePlayer: {}", player.name)
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

    /**
     * 获取所有玩家
     */
    fun getPlayers(): Collection<GamePlayer> {
        return byPlayerId.values
    }

    fun removePlayer(player: GamePlayer) {
        Logger.info("Remove GamePlayer: {}", player.name)
        byUniqueId.remove(player.uniqueId)
        byPlayerId.remove(player.playerId)
    }
}
