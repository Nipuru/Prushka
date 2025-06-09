package server.broker.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import net.afyer.afybroker.server.Broker
import server.common.message.GetPlayerLocationRequest
import server.common.message.LocationMessage
import server.common.message.TeleportInvokeRequest
import server.common.message.TeleportOrSpawnRequest


/**
 * @author Nipuru
 * @since 2024/11/20 14:57
 */
class TeleportInvokeBrokerProcessor: AsyncUserProcessor<TeleportInvokeRequest>() {
    override fun handleRequest(context: BizContext, asyncContext: AsyncContext, message: TeleportInvokeRequest) {
        val from = Broker.getPlayer(message.from) ?: return
        val fromServer = from.server ?: return
        val to = Broker.getPlayer(message.to) ?: return
        val toServer = to.server ?: return
        val location = toServer.invokeSync<LocationMessage?>(GetPlayerLocationRequest(to.name)) ?: return
        toServer.oneway(TeleportOrSpawnRequest(from.name, location))
        if (fromServer != toServer) {
            from.connectToServer(toServer.name)
        }
    }

    override fun interest(): String {
        return TeleportInvokeRequest::class.java.name
    }
}
