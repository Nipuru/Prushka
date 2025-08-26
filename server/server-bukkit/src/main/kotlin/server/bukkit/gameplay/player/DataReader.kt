package server.bukkit.gameplay.player

import net.afyer.afybroker.client.Broker
import server.common.message.PlayerDataMessage
import server.common.message.PlayerDataTransferRequest
import server.common.service.PlayerService
import java.net.InetAddress
import java.util.*

object DataReader {

    private val playerService = Broker.getService(PlayerService::class.java)

    fun read(name: String, uniqueId: UUID, address: InetAddress): GamePlayer {
        // 优先从别的服务器转移数据
        val transferRequest = PlayerDataTransferRequest(uniqueId)
        val playerData = Broker.invokeSync<PlayerDataMessage>(transferRequest)

        if (playerData != null) {
            val dataInfo = DataInfo(playerData.data)
            val gamePlayer = GamePlayer(playerData.playerId, playerData.dbId, name, uniqueId)
            gamePlayer.unpack(dataInfo)
            return gamePlayer
        } else {
            // 其他服务器没有数据代表登录 需要新建数据或从数据库加载
            val playerMessage = playerService.login(name, uniqueId, address.hostAddress)
            val player = GamePlayer(playerMessage.playerId, playerMessage.dbId, name, uniqueId)
            val tableInfos = TableInfos()
            player.preload(tableInfos)
            val data = player.dataService.queryPlayer(playerMessage.playerId, tableInfos.tables)
            player.unpack(DataInfo(data))
            return player
        }
    }
}
