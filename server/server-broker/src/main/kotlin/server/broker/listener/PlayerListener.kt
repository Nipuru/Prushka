package server.broker.listener

import net.afyer.afybroker.server.event.PlayerProxyLoginEvent
import net.afyer.afybroker.server.event.PlayerProxyLogoutEvent
import net.afyer.afybroker.server.plugin.EventHandler
import net.afyer.afybroker.server.plugin.Listener
import server.broker.player.GamePlayer
import server.broker.player.GamePlayers

class PlayerListener : Listener {

    @EventHandler
    fun onLogin(event: PlayerProxyLoginEvent) {
        val player = GamePlayer(event.player)
        GamePlayers.registerPlayer(player)
    }

    @EventHandler
    fun onEvent(event: PlayerProxyLogoutEvent) {
        val player = GamePlayers.getPlayer(event.player.name) ?: return
        GamePlayers.removePlayer(player)
    }
}
