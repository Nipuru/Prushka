package server.broker.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import net.afyer.afybroker.server.Broker
import server.common.message.SystemChatMessage
import server.common.message.SystemChatMessage.Message


/**
 * @author Nipuru
 * @since 2025/09/14 17:05
 */
class SystemChatBrokerProcessor : AsyncUserProcessor<SystemChatMessage>() {
    override fun handleRequest(bizContext: BizContext, asyncContext: AsyncContext, request: SystemChatMessage) {
        val serverMessages = mutableMapOf<String, MutableList<Message>>()
        for (message in request.messages) {
            val server = Broker.getPlayer(message.receiver)?.server ?: continue
            val messages = serverMessages.getOrPut(server.name) { mutableListOf() }
            messages += message
        }
        for ((serverName, messages) in serverMessages) {
            val server = Broker.getClient(serverName) ?: continue
            server.oneway(SystemChatMessage(messages))
        }
    }

    override fun interest(): String {
        return SystemChatMessage::class.java.name
    }
}