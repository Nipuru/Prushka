package server.broker.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import net.afyer.afybroker.server.Broker
import server.common.message.AudienceMessage
import server.common.message.AudienceMessage.Message


/**
 * @author Nipuru
 * @since 2025/09/14 17:05
 */
class AudienceBrokerProcessor : AsyncUserProcessor<AudienceMessage>() {
    override fun handleRequest(bizContext: BizContext, asyncContext: AsyncContext, request: AudienceMessage) {
        val serverMessages = mutableMapOf<String, MutableList<Message>>()
        for (message in request.messages) {
            val server = Broker.getPlayer(message.receiver)?.server ?: continue
            val messages = serverMessages.getOrPut(server.name) { mutableListOf() }
            messages += message
        }
        for ((serverName, messages) in serverMessages) {
            val server = Broker.getClient(serverName) ?: continue
            server.oneway(AudienceMessage(messages))
        }
    }

    override fun interest(): String {
        return AudienceMessage::class.java.name
    }
}