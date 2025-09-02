package server.bukkit.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import org.bukkit.Bukkit
import server.bukkit.BukkitPlugin
import server.bukkit.gameplay.player.DataInfo
import server.bukkit.gameplay.player.gamePlayer
import server.bukkit.nms.quit
import server.bukkit.util.schedule
import server.common.message.PlayerDataMessage
import server.common.message.PlayerDataTransferRequest

class PlayerDataTransferBukkitProcessor : AsyncUserProcessor<PlayerDataTransferRequest>() {

    override fun handleRequest(bizCtx: BizContext, asyncCtx: AsyncContext, request: PlayerDataTransferRequest) {
        BukkitPlugin.schedule {
            asyncCtx.sendResponse(handle(request))
        }
    }

    private fun handle(request: PlayerDataTransferRequest): Any? {
        val bukkitPlayer = Bukkit.getPlayer(request.uniqueId) ?: return null
        val player = bukkitPlayer.gamePlayer

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
