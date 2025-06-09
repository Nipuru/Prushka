package server.bukkit.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import server.bukkit.gameplay.player.GamePlayers


/**
 * @author Nipuru
 * @since 2024/11/12 15:40
 */
class PlayerMoveListener : Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onEvent(event: PlayerMoveEvent) {
        //解除afk
        val player = GamePlayers.getPlayer(event.player.uniqueId)
        player.core.afk = false
        player.teleport.setLastLocation(event.to)
    }
}
