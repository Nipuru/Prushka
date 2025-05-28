package top.nipuru.prushka.game.listener

import top.nipuru.prushka.game.gameplay.player.GamePlayers
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent


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
