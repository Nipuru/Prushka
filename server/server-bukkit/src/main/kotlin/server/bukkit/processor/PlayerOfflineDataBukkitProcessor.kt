package server.bukkit.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import server.bukkit.BukkitPlugin
import server.bukkit.gameplay.player.GamePlayerManager
import server.bukkit.util.schedule
import server.common.message.PlayerOfflineDataMessage

class PlayerOfflineDataBukkitProcessor : AsyncUserProcessor<PlayerOfflineDataMessage>() {

    override fun handleRequest(bizContext: BizContext, asyncContext: AsyncContext, request: PlayerOfflineDataMessage) {
        BukkitPlugin.schedule {
            asyncContext.sendResponse(handle(request))
        }
    }

    private fun handle(request: PlayerOfflineDataMessage) : Boolean {
        val gamePlayer = GamePlayerManager.getPlayer(request.playerId)
        val handler = gamePlayer.offline.getHandler(request.module) ?: return false
        val result = handler.handle(request.data, true)
        return result
    }

    override fun interest(): String {
        return PlayerOfflineDataMessage::class.java.name
    }
}
