package server.shared.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import server.common.message.OnlinePlayersMessage
import server.shared.service.PlayerInfoServiceImpl


/**
 * @author Nipuru
 * @since 2025/09/08 20:21
 */
class OnlinePlayersSharedProcessor : AsyncUserProcessor<OnlinePlayersMessage>() {
    override fun handleRequest(context: BizContext, asyncContext: AsyncContext, message: OnlinePlayersMessage) {
        var onlinePlayers = PlayerInfoServiceImpl.onlinePlayers
        onlinePlayers.removeAll(message.offlineList)
        onlinePlayers.addAll(message.onlineList)
    }

    override fun interest(): String {
        return OnlinePlayersMessage::class.java.name
    }
}