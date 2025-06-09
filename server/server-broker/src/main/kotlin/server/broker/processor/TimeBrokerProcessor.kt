package server.broker.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import com.alipay.remoting.rpc.protocol.SyncUserProcessor
import net.afyer.afybroker.core.BrokerClientType
import net.afyer.afybroker.server.Broker
import server.common.ClientTag
import server.common.message.DebugTimeNotify
import server.common.message.GetTimeRequest


/**
 * @author Nipuru
 * @since 2024/11/28 10:08
 */
class GetTimeBrokerProcessor : SyncUserProcessor<GetTimeRequest>() {
    override fun handleRequest(context: BizContext, message: GetTimeRequest): Long {
        // 向公共服获取时间
        val name = server.common.ClientType.SHARED
        val server = Broker.getClient(name)!!
        return server.invokeSync(message)!!
    }

    override fun interest(): String {
        return GetTimeRequest::class.java.name
    }
}

class DebugTimeBrokerProcessor : AsyncUserProcessor<DebugTimeNotify>() {
    override fun handleRequest(context: BizContext, asyncContext: AsyncContext, message: DebugTimeNotify) {
        Broker.getClientManager().getByType(BrokerClientType.SERVER)
            .filter { it.hasTag(ClientTag.GAME) }
            .forEach { it.oneway(message) }
    }

    override fun interest(): String {
        return DebugTimeNotify::class.java.name
    }
}