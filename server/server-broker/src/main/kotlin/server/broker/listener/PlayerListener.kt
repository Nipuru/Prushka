package server.broker.listener

import net.afyer.afybroker.server.Broker
import net.afyer.afybroker.server.event.ClientRegisterEvent
import net.afyer.afybroker.server.event.PlayerProxyLoginEvent
import net.afyer.afybroker.server.event.PlayerProxyLogoutEvent
import net.afyer.afybroker.server.plugin.EventHandler
import net.afyer.afybroker.server.plugin.Listener
import server.broker.player.GamePlayer
import server.broker.player.GamePlayers
import server.common.ClientType
import server.common.message.OnlinePlayersMessage

class PlayerListener : Listener {

    @EventHandler
    fun onRegister(event: ClientRegisterEvent) {
        // 公共服连接时发送玩家列表
        val client = event.brokerClientItem
        if (client.type == ClientType.SHARED) {
            client.oneway(OnlinePlayersMessage(onlineList = GamePlayers.players.map { it.name }))
        }
    }

    @EventHandler
    fun onLogin(event: PlayerProxyLoginEvent) {
        val player = GamePlayer(event.player)
        GamePlayers.registerPlayer(player)

        Broker.getClientManager().getByType(ClientType.SHARED).forEach {
            it.oneway(OnlinePlayersMessage(onlineList = listOf(player.name)))
        }
    }

    @EventHandler
    fun onEvent(event: PlayerProxyLogoutEvent) {
        val player = GamePlayers.getPlayer(event.player.name) ?: return
        GamePlayers.removePlayer(player)

        Broker.getClientManager().getByType(ClientType.SHARED).forEach {
            it.oneway(OnlinePlayersMessage(offlineList = listOf(player.name)))
        }
    }
}
