package top.nipuru.prushka.server.game.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import top.nipuru.prushka.server.common.message.PlayerDataMessage
import top.nipuru.prushka.server.common.message.PlayerDataTransferMessage
import top.nipuru.prushka.server.game.gameplay.player.DataInfo
import top.nipuru.prushka.server.game.nms.freeze
import top.nipuru.prushka.server.game.nms.quit
import top.nipuru.prushka.server.game.gameplay.player.GamePlayers
import top.nipuru.prushka.server.game.util.submit
import org.bukkit.Bukkit

class PlayerDataTransferGameProcessor : AsyncUserProcessor<PlayerDataTransferMessage>() {

    override fun handleRequest(bizCtx: BizContext, asyncCtx: AsyncContext, request: PlayerDataTransferMessage) {
        submit(async = false) {
            asyncCtx.sendResponse(handle(request))
        }
    }

    private fun handle(request: PlayerDataTransferMessage): Any? {
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
        return PlayerDataTransferMessage::class.java.name
    }
}
