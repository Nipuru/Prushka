package top.nipuru.prushka.game.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import top.nipuru.prushka.common.message.PlayerOfflineDataMessage
import top.nipuru.prushka.game.gameplay.player.GamePlayer
import top.nipuru.prushka.game.gameplay.player.GamePlayers
import top.nipuru.prushka.game.plugin
import top.nipuru.prushka.game.util.submit
import org.bukkit.Bukkit

class PlayerOfflineDataGameProcessor : AsyncUserProcessor<PlayerOfflineDataMessage>() {

    override fun handleRequest(bizContext: BizContext, asyncContext: AsyncContext, request: PlayerOfflineDataMessage) {
        submit(async = false) {
            asyncContext.sendResponse(handle(request))
        }
    }

    private fun handle(request: PlayerOfflineDataMessage) : Boolean {
        val gamePlayer: GamePlayer = GamePlayers.getPlayer(request.playerId)
        val handler = gamePlayer.offline.getHandler(request.module) ?: return false
        val result = handler.handle(request.data, true)
        return result
    }

    override fun interest(): String {
        return PlayerOfflineDataMessage::class.java.name
    }
}
