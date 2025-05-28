package top.nipuru.prushka.server.broker.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import net.afyer.afybroker.core.util.AbstractInvokeCallback
import net.afyer.afybroker.server.Broker
import top.nipuru.prushka.server.broker.logger.logger
import top.nipuru.prushka.server.common.ClientTag
import top.nipuru.prushka.server.common.message.PlayerDataTransferMessage

class PlayerDataTransferBrokerProcessor : AsyncUserProcessor<PlayerDataTransferMessage>() {

    override fun handleRequest(bizCtx: BizContext, asyncCtx: AsyncContext, request: PlayerDataTransferMessage) {
        val player = Broker.getPlayer(request.uniqueId)
        if (player == null) {
            asyncCtx.sendResponse(null)
            return
        }
        val currentServer = player.server
        if (currentServer == null || !currentServer.hasTag(ClientTag.GAME)) {
            asyncCtx.sendResponse(null)
            return
        }

        val fromServer = Broker.getClient(bizCtx)
        if (currentServer == fromServer) {
            asyncCtx.sendResponse(null)
            return
        }

        // 收到消息后将消息转发给玩家所在的bukkit服务器
        currentServer.invokeWithCallback(request, object : AbstractInvokeCallback() {
            override fun onResponse(result: Any?) {
                asyncCtx.sendResponse(result)
            }

            override fun onException(e: Throwable) {
                logger.error(e.message, e)
                asyncCtx.sendException(e)
            }
        })
    }

    override fun interest(): String {
        return PlayerDataTransferMessage::class.java.name
    }
}
