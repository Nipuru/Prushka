package top.nipuru.minegame.broker.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import net.afyer.afybroker.core.util.AbstractInvokeCallback
import net.afyer.afybroker.server.Broker
import top.nipuru.minegame.broker.logger.logger
import top.nipuru.minegame.common.ClientTag
import top.nipuru.minegame.common.message.PlayerDataTransferRequest

class PlayerDataTransferBrokerProcessor : AsyncUserProcessor<PlayerDataTransferRequest>() {

    override fun handleRequest(bizCtx: BizContext, asyncCtx: AsyncContext, request: PlayerDataTransferRequest) {
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
        return PlayerDataTransferRequest::class.java.name
    }
}
