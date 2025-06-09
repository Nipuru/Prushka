package server.bukkit.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import server.bukkit.gameplay.player.GamePlayers


/**
 * @author Nipuru
 * @since 2024/11/12 15:40
 */
class PlayerCommandListener : Listener {

    @EventHandler
    fun onPlayerCommand(event: PlayerCommandPreprocessEvent) {
        //解除afk
        val player = GamePlayers.getPlayer(event.player.uniqueId)
        player.core.afk = false
    }
}
