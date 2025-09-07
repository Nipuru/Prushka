package server.bukkit.listener

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import server.bukkit.gameplay.player.GamePlayerManager
import server.bukkit.nms.PacketListener
import server.bukkit.nms.addChannelHandler
import server.common.logger.Logger

class PlayerJoinListener : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onEventLowest(event: PlayerJoinEvent) {
        event.joinMessage(null)
        event.player.addChannelHandler(PacketListener(event.player))
        // GamePlayer 在 PreLogin 的时候就应该加载好了
        try {
            val player = GamePlayerManager.removePendingPlayer(event.player.uniqueId)
                ?: throw NullPointerException("Player " + event.player.name + " has no pending data")
            GamePlayerManager.register(player)
            // 玩家加入服务器
            player.init()
            player.onJoin()
        } catch (e: Exception) {
            val message = Component.text("加入游戏失败！请重试").color(NamedTextColor.RED)
            event.player.kick(message)
            Logger.error(e.message, e)
        }
    }
}
