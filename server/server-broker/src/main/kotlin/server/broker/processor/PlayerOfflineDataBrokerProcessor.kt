package server.broker.processor

import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.SyncUserProcessor
import net.afyer.afybroker.server.Broker
import net.afyer.afybroker.server.proxy.BrokerClientItem
import server.broker.player.ServerPlayerManager
import server.common.ClientTag
import server.common.ClientType
import server.common.message.PlayerOfflineDataMessage

class PlayerOfflineDataBrokerProcessor : SyncUserProcessor<PlayerOfflineDataMessage>() {

    override fun handleRequest(bizContext: BizContext, request: PlayerOfflineDataMessage): Boolean {
        // 如果在线处理了则不需要新增离线消息
        if (onlineRequest(request)) return true

        // 新增离线消息
        return offlineRequest(request)
    }

    private fun onlineRequest(request: PlayerOfflineDataMessage): Boolean {
        val player = ServerPlayerManager.getPlayer(request.name)

        val bukkit = player?.brokerPlayer?.server ?: return false
        if (!bukkit.hasTag(ClientTag.GAME)) return false

        return bukkit.invokeSync(request)
    }

    private fun offlineRequest(request: PlayerOfflineDataMessage): Boolean {
        val name = String.format("%s-%d", ClientType.DB, request.dbId)
        val dbServer: BrokerClientItem = Broker.getClient(name) ?: return false

        return dbServer.invokeSync(request)
    }

    override fun interest(): String {
        return PlayerOfflineDataMessage::class.java.name
    }
}
