package server.broker.listener

import net.afyer.afybroker.server.Broker
import net.afyer.afybroker.server.event.ClientRegisterEvent
import net.afyer.afybroker.server.event.PlayerProxyLoginEvent
import net.afyer.afybroker.server.event.PlayerProxyLogoutEvent
import net.afyer.afybroker.server.plugin.EventHandler
import net.afyer.afybroker.server.plugin.Listener
import server.broker.player.ServerPlayer
import server.broker.player.ServerPlayerManager
import server.common.ClientType
import server.common.message.OnlinePlayersMessage

class PlayerListener : Listener {

    @EventHandler
    fun onRegister(event: ClientRegisterEvent) {
        // 公共服连接时发送玩家列表
        val client = event.brokerClientItem
        if (client.type == ClientType.SHARED) {
            client.oneway(OnlinePlayersMessage(onlineList = ServerPlayerManager.players.map { it.name }))
        }
    }

    @EventHandler
    fun onLogin(event: PlayerProxyLoginEvent) {
        val player = ServerPlayer(event.player)
        ServerPlayerManager.registerPlayer(player)

        Broker.getClientManager().getByType(ClientType.SHARED).forEach {
            it.oneway(OnlinePlayersMessage(onlineList = listOf(player.name)))
        }
    }

    @EventHandler
    fun onEvent(event: PlayerProxyLogoutEvent) {
        val player = ServerPlayerManager.getPlayer(event.player.name) ?: return
        ServerPlayerManager.removePlayer(player)

        Broker.getClientManager().getByType(ClientType.SHARED).forEach {
            it.oneway(OnlinePlayersMessage(offlineList = listOf(player.name)))
        }
    }
}
