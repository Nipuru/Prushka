package server.bukkit.processor

import org.bukkit.Bukkit
import server.bukkit.util.component
import server.bukkit.util.submit
import server.common.message.game.KickPlayerMessage
import server.common.message.game.KickReason
import server.common.processor.RequestDispatcher
import server.common.processor.RequestDispatcher.ResponseContext


/**
 * @author Nipuru
 * @since 2024/12/03 16:52
 */
class KickPlayerHandler : RequestDispatcher.Handler<KickPlayerMessage> {
    override fun handle(asyncCtx: ResponseContext, request: KickPlayerMessage) {
        submit(async = false) {
            asyncCtx.sendResponse(handle(request))
        }
    }

    private fun handle(request: KickPlayerMessage) : Boolean {
        val player = Bukkit.getPlayer(request.uniqueId) ?: return false
        val message = when(request.reason) {
            KickReason.SYSTEM_ERROR -> "服务器系统错误"
            KickReason.FORBIDDEN -> "你的账号被封停，请联系管理员"
            KickReason.REPAIR -> "服务器维护中，请稍后再进入游戏"
        }
        player.kick(message.component())
        return true
    }

    override fun interest(): Class<KickPlayerMessage> {
        return KickPlayerMessage::class.java
    }
}
