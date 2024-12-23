package top.nipuru.prushka.game.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import top.nipuru.prushka.common.message.PlayerDataMessage
import top.nipuru.prushka.common.message.PlayerDataTransferRequest
import top.nipuru.prushka.game.gameplay.player.DataInfo
import top.nipuru.prushka.game.gameplay.player.GamePlayer
import top.nipuru.prushka.game.logger.logger
import top.nipuru.prushka.game.nms.freeze
import top.nipuru.prushka.game.nms.quit
import top.nipuru.prushka.game.gameplay.player.GamePlayers
import top.nipuru.prushka.game.plugin
import top.nipuru.prushka.game.util.submit
import org.bukkit.Bukkit

class PlayerDataTransferGameProcessor : AsyncUserProcessor<PlayerDataTransferRequest>() {

    override fun handleRequest(bizCtx: BizContext, asyncCtx: AsyncContext, request: PlayerDataTransferRequest) {
        submit(async = false) {
            asyncCtx.sendResponse(handle(request))
        }
    }

    private fun handle(request: PlayerDataTransferRequest): Any? {
        val bukkitPlayer = Bukkit.getPlayer(request.uniqueId) ?: return null
        val player = GamePlayers.getPlayer(bukkitPlayer.uniqueId)

        player.bukkitPlayer.freeze() // 冻结玩家 不处理客户端发包
        player.bukkitPlayer.quit()   // 强制移出玩家列表 触发 PlayerQuitEvent 并完成数据的保存

        val dataInfo = DataInfo(HashMap())
        player.pack(dataInfo)
        val response = PlayerDataMessage(player.playerId, player.dbId, dataInfo.tables)
        return response
    }

    override fun interest(): String {
        return PlayerDataTransferRequest::class.java.name
    }
}
