package server.bukkit.gameplay.player

import net.afyer.afybroker.client.Broker
import server.common.message.PlayerDataTransferRequest
import server.common.message.PlayerDataMessage
import server.common.service.PlayerDataService
import server.common.service.PlayerLoginService
import java.net.InetAddress
import java.util.*

object DataReader {
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
            val playerLoginService = Broker.getService(PlayerLoginService::class.java)

            val loginData = playerLoginService.login(name, uniqueId, address.hostAddress)

            val gamePlayer = GamePlayer(loginData.playerId, loginData.dbId, name, uniqueId)
            val tableInfos = TableInfos()
            gamePlayer.preload(tableInfos)
            val playerDataService = Broker.getService(PlayerDataService::class.java, loginData.dbId.toString())
            val data = playerDataService.queryPlayer(loginData.playerId, tableInfos.tables)
            gamePlayer.unpack(DataInfo(data))
            return gamePlayer
        }
    }
}
