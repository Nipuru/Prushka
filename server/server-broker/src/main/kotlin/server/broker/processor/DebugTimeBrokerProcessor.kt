package server.broker.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import net.afyer.afybroker.core.BrokerClientType
import net.afyer.afybroker.server.Broker
import server.common.ClientTag
import server.common.message.DebugTimeNotify


/**
 * @author Nipuru
 * @since 2024/11/28 10:08
 */
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