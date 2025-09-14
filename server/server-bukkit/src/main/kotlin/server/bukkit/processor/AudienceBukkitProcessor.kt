package server.bukkit.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import org.bukkit.Bukkit
import server.bukkit.util.text.TextFactory
import server.common.message.SystemChatMessage


/**
 * @author Nipuru
 * @since 2025/09/14 17:05
 */
class SystemChatBukkitProcessor : AsyncUserProcessor<SystemChatMessage>() {
    override fun handleRequest(bizContext: BizContext, asyncContext: AsyncContext, request: SystemChatMessage) {
        request.messages.forEach { message ->
            Bukkit.getPlayer(message.receiver)?.apply {
                sendMessage(TextFactory.instance.miniMessage.deserialize(message.message))
            }
        }
    }

    override fun interest(): String {
        return SystemChatMessage::class.java.name
    }

}