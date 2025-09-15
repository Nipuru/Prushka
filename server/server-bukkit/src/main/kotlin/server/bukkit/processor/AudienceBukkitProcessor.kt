package server.bukkit.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import server.bukkit.util.text.TextFactory
import server.common.message.AudienceMessage
import server.common.message.AudienceMessage.Message.SystemChat


/**
 * @author Nipuru
 * @since 2025/09/14 17:05
 */
class AudienceBukkitProcessor : AsyncUserProcessor<AudienceMessage>() {

    private val miniMessage get() = TextFactory.instance.miniMessage

    override fun handleRequest(bizContext: BizContext, asyncContext: AsyncContext, request: AudienceMessage) {
        request.messages.forEach { message ->
            Bukkit.getPlayer(message.receiver)?.apply { send(message) }
        }
    }

    private fun Audience.send(message: AudienceMessage.Message) = when(message) {
        is SystemChat -> sendMessage(miniMessage.deserialize(message.message))
    }

    override fun interest(): String {
        return AudienceMessage::class.java.name
    }

}