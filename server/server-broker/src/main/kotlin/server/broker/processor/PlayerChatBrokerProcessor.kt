package server.broker.processor

import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.SyncUserProcessor
import net.afyer.afybroker.core.BrokerClientType
import net.afyer.afybroker.server.Broker
import server.common.ClientTag
import server.common.message.PlayerChatMessage
import server.common.message.PlayerPrivateChatMessage

class PlayerChatBrokerProcessor : SyncUserProcessor<PlayerChatMessage>() {
    override fun handleRequest(bizContext: BizContext, request: PlayerChatMessage): Any {
        for (client in Broker.getClientManager().list()) {
            if (client.type != BrokerClientType.SERVER) continue
            if (!client.hasTag(ClientTag.GAME)) continue
            client.oneway(request)
        }
        return true
    }

    override fun interest(): String {
        return PlayerChatMessage::class.java.name
    }
}

class PlayerPrivateChatBrokerProcessor : SyncUserProcessor<PlayerPrivateChatMessage>() {
    override fun handleRequest(bizContext: BizContext, request: PlayerPrivateChatMessage): Any {
        val fromServer = Broker.getClient(bizContext) ?: return false
        val toServer = Broker.getPlayer(request.receiver)?.server ?: return false
        if (fromServer != toServer) {
            fromServer.oneway(request)  // 给发送者的服务器也发送一条
        }
        return toServer.invokeSync(request)
    }

    override fun interest(): String {
        return PlayerPrivateChatMessage::class.java.name
    }
}

