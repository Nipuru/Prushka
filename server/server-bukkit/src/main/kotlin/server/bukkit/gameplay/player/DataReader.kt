package server.bukkit.gameplay.player

import net.afyer.afybroker.client.Broker
import server.bukkit.route.authRequest
import server.bukkit.route.databaseRequest
import server.common.message.PlayerDataTransferRequest
import server.common.message.auth.PlayerLoginResponse
import server.common.message.auth.PlayerLoginRequest
import server.common.message.database.PlayerDataMessage
import server.common.message.database.PlayerDataQueryRequest
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
            val loginRequest = PlayerLoginRequest(name, uniqueId, address.hostAddress)
            val loginResponse = authRequest<PlayerLoginResponse>(loginRequest)!!
            val queryRequest = PlayerDataQueryRequest(loginResponse.playerId)
            val gamePlayer = GamePlayer(loginResponse.playerId, loginResponse.dbId, name, uniqueId)
            gamePlayer.preload(queryRequest)
            val dataInfo = DataInfo(databaseRequest(loginResponse.dbId, queryRequest)!!)
            gamePlayer.unpack(dataInfo)
            return gamePlayer
        }
    }
}
