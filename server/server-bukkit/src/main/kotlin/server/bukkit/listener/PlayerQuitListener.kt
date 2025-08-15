package server.bukkit.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import server.bukkit.gameplay.player.GamePlayerManager
import server.bukkit.gameplay.player.gamePlayer

class PlayerQuitListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onEventMonitor(event: PlayerQuitEvent) {
        event.quitMessage(null)
        val player = event.player.gamePlayer
        // 由于存在跨服机制 所以 Quit 可能被触发多次 但只有第一次有效
        if (player.bukkitPlayer.isOnline) {
            // 玩家退出方法
            player.onQuit()
            GamePlayerManager.removePlayer(player)
        }
    }
}
