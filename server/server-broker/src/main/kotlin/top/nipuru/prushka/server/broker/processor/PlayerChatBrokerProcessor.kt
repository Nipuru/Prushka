package top.nipuru.prushka.server.broker.processor

import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.SyncUserProcessor
import net.afyer.afybroker.core.BrokerClientType
import net.afyer.afybroker.server.Broker
import top.nipuru.prushka.server.broker.player.GamePlayers
import top.nipuru.prushka.server.common.ClientTag
import top.nipuru.prushka.server.common.message.PlayerChatMessage
import top.nipuru.prushka.server.common.message.PlayerPrivateChatMessage

class PlayerChatBrokerProcessor : SyncUserProcessor<PlayerChatMessage>() {

    override fun handleRequest(bizContext: BizContext, request: PlayerChatMessage): Any {
        val player = GamePlayers.getPlayer(request.sender.name)!!
        val chatLimiter = player.chatLimiter
        if (chatLimiter.isLimit) {
            return PlayerChatMessage.RATE_LIMIT
        }
        for (client in Broker.getClientManager().list()) {
            if (client.type != BrokerClientType.SERVER) continue
            if (!client.hasTag(ClientTag.GAME)) continue
            client.oneway(request)
        }
        return PlayerChatMessage.SUCCESS
    }

    override fun interest(): String {
        return PlayerChatMessage::class.java.name
    }
}

class PlayerPrivateChatBrokerProcessor : SyncUserProcessor<PlayerPrivateChatMessage>() {

    override fun handleRequest(bizContext: BizContext, request: PlayerPrivateChatMessage): Any {
        val player = GamePlayers.getPlayer(request.sender.name)!!
        val chatLimiter = player.chatLimiter
        if (chatLimiter.isLimit) {
            return PlayerPrivateChatMessage.RATE_LIMIT
        }
        val receiver = Broker.getPlayer(request.receiver)
        if (receiver == null || receiver.server == null) {
            return PlayerPrivateChatMessage.NOT_ONLINE
        }
        return receiver.server!!.invokeSync(request)
    }

    override fun interest(): String {
        return PlayerPrivateChatMessage::class.java.name
    }
}

