package server.broker.processor

import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.SyncUserProcessor
import net.afyer.afybroker.server.Broker
import server.common.message.GetPlayerLocationRequest
import server.common.message.LocationMessage
import server.common.message.TeleportInvokeRequest
import server.common.message.TeleportOrSpawnRequest


/**
 * @author Nipuru
 * @since 2024/11/20 14:57
 */
class TeleportInvokeBrokerProcessor: SyncUserProcessor<TeleportInvokeRequest>() {
    override fun handleRequest(context: BizContext?, message: TeleportInvokeRequest): Boolean {
        val to = Broker.getPlayer(message.to) ?: return false
        val toServer = to.server ?: return false
        val location = toServer.invokeSync<LocationMessage>(GetPlayerLocationRequest(to.name)) ?: return false
        toServer.oneway(TeleportOrSpawnRequest(message.froms, location))
        var success = false
        for (name in message.froms) {
            val from = Broker.getPlayer(name) ?: continue
            // 服务器不一致时进行跳转
            if (from.server != toServer) {
                from.connectToServer(toServer.name)
            }
            success = true
        }
        return success
    }

    override fun interest(): String {
        return TeleportInvokeRequest::class.java.name
    }
}
