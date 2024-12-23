package top.nipuru.prushka.game.gameplay.player

import net.afyer.afybroker.client.Broker
import top.nipuru.prushka.common.message.PlayerDataMessage
import top.nipuru.prushka.common.message.PlayerDataTransferRequest
import top.nipuru.prushka.common.message.auth.QueryUserRequest
import top.nipuru.prushka.common.message.auth.UserMessage
import top.nipuru.prushka.common.message.database.QueryPlayerRequest
import top.nipuru.prushka.game.route.authRequest
import top.nipuru.prushka.game.route.databaseRequest
import java.net.InetAddress
import java.util.*

object DataReader {
    fun read(name: String, uniqueId: UUID, address: InetAddress): GamePlayer {
        // 优先从别的服务器转移数据
        val transferRequest = PlayerDataTransferRequest(uniqueId)
        val playerDataMessage = Broker.invokeSync<PlayerDataMessage>(transferRequest)

        if (playerDataMessage != null) {
            val dataInfo = DataInfo(playerDataMessage.data)
            val gamePlayer = GamePlayer(playerDataMessage.playerId, playerDataMessage.dbId, name, uniqueId)
            gamePlayer.unpack(dataInfo)
            return gamePlayer
        } else {
            // 其他服务器没有数据代表登录 需要新建数据或从数据库加载
            val queryUserRequest = QueryUserRequest(name, uniqueId, address.hostAddress)
            val userMessage = authRequest<UserMessage>(queryUserRequest)!!
            val queryPlayerRequest = QueryPlayerRequest(userMessage.playerId)
            val gamePlayer = GamePlayer(userMessage.playerId, userMessage.dbId, name, uniqueId)
            gamePlayer.preload(queryPlayerRequest)
            val dataInfo = DataInfo(databaseRequest(userMessage.dbId, queryPlayerRequest)!!)
            gamePlayer.unpack(dataInfo)
            return gamePlayer
        }
    }
}
