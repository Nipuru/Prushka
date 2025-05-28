package top.nipuru.prushka.server.game.listener

import top.nipuru.prushka.server.game.gameplay.player.GamePlayer
import top.nipuru.prushka.server.game.logger.logger
import top.nipuru.prushka.server.game.gameplay.player.GamePlayers
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*

class PlayerJoinListener(private val pendingPlayers: MutableMap<UUID, GamePlayer>) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onEventLowest(event: PlayerJoinEvent) {
        event.joinMessage(null)

        // GamePlayer 在 PreLogin 的时候就应该加载好了
        try {
            val gamePlayer = pendingPlayers.remove(event.player.uniqueId)
                ?: throw NullPointerException("Player " + event.player.name + " has no pending data")
            GamePlayers.register(gamePlayer)
            // 玩家加入服务器
            gamePlayer.init()
            gamePlayer.onJoin()
        } catch (e: Exception) {
            val message = Component.text("加入游戏失败！请重试").color(NamedTextColor.RED)
            event.player.kick(message)
            logger.error(e.message, e)
        }
    }
}
