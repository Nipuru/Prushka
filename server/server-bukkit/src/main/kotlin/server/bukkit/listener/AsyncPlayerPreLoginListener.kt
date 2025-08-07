package server.bukkit.listener

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import server.bukkit.enableLatch
import server.bukkit.gameplay.player.DataReader
import server.bukkit.gameplay.player.GamePlayer
import server.common.logger.logger
import java.util.*

class AsyncPlayerPreLoginListener(private val pendingPlayers: MutableMap<UUID, GamePlayer>) : Listener {

    @EventHandler(priority = EventPriority.LOW)
    fun onEventLow(event: AsyncPlayerPreLoginEvent) {

        try {
            // 防止在插件启用之前玩家加入进来
            enableLatch.await()
            val player = DataReader.read(event.name, event.uniqueId, event.address)
            pendingPlayers[event.uniqueId] = player
        } catch (e: Exception) {
            val message = Component.text("登录失败！请重试").color(NamedTextColor.RED)
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message)
            logger.error(e.message, e)
        }
    }
}
