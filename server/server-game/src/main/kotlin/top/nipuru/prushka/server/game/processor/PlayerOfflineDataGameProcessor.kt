package top.nipuru.prushka.server.game.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import top.nipuru.prushka.server.common.message.PlayerOfflineDataMessage
import top.nipuru.prushka.server.game.gameplay.player.GamePlayer
import top.nipuru.prushka.server.game.gameplay.player.GamePlayers
import top.nipuru.prushka.server.game.plugin
import top.nipuru.prushka.server.game.util.submit
import org.bukkit.Bukkit

class PlayerOfflineDataGameProcessor : AsyncUserProcessor<top.nipuru.prushka.server.common.message.PlayerOfflineDataMessage>() {

    override fun handleRequest(bizContext: BizContext, asyncContext: AsyncContext, request: top.nipuru.prushka.server.common.message.PlayerOfflineDataMessage) {
        submit(async = false) {
            asyncContext.sendResponse(handle(request))
        }
    }

    private fun handle(request: top.nipuru.prushka.server.common.message.PlayerOfflineDataMessage) : Boolean {
        val gamePlayer: GamePlayer = GamePlayers.getPlayer(request.playerId)
        val handler = gamePlayer.offline.getHandler(request.module) ?: return false
        val result = handler.handle(request.data, true)
        return result
    }

    override fun interest(): String {
        return top.nipuru.prushka.server.common.message.PlayerOfflineDataMessage::class.java.name
    }
}
