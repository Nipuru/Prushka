package server.bukkit.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import server.bukkit.gameplay.player.GamePlayers

class PlayerQuitListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onEventMonitor(event: PlayerQuitEvent) {
        event.quitMessage(null)
        val player = event.player
        // 由于存在跨服机制 所以 Quit 可能被触发多次 但只有第一次有效
        if (player.isOnline) {
            val gamePlayer = GamePlayers.getPlayer(player.uniqueId)
            // 玩家退出方法
            gamePlayer.onQuit()
            GamePlayers.removePlayer(gamePlayer)
        }
    }
}
