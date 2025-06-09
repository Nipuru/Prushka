package top.nipuru.prushka.server.game.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import org.bukkit.Bukkit
import server.bukkit.gameplay.player.DataInfo
import server.bukkit.gameplay.player.GamePlayers
import server.bukkit.nms.freeze
import server.bukkit.nms.quit
import server.bukkit.util.submit
import server.common.message.PlayerDataMessage
import server.common.message.PlayerDataTransferMessage

class PlayerDataTransferBukkitProcessor : AsyncUserProcessor<PlayerDataTransferMessage>() {

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
