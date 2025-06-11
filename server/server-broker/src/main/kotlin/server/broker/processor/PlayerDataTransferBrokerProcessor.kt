package server.broker.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import net.afyer.afybroker.core.util.AbstractInvokeCallback
import net.afyer.afybroker.server.Broker
import server.broker.logger.logger
import server.common.ClientTag
import server.common.message.PlayerDataTransferRequest

class PlayerDataTransferBrokerProcessor : AsyncUserProcessor<PlayerDataTransferRequest>() {

    override fun handleRequest(bizCtx: BizContext, asyncCtx: AsyncContext, request: PlayerDataTransferRequest) {
        val currentServer = Broker.getPlayer(request.uniqueId)?.server
        val fromServer = Broker.getClient(bizCtx)
        if (currentServer == null || !currentServer.hasTag(ClientTag.GAME) || currentServer == fromServer) {
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
        return PlayerDataTransferRequest::class.java.name
    }
}
