package top.nipuru.prushka.game.gameplay.player

import net.afyer.afybroker.client.Broker
import top.nipuru.prushka.common.message.PlayerDataMessage
import top.nipuru.prushka.common.message.PlayerDataTransferMessage
import top.nipuru.prushka.common.message.auth.PlayerRequestMessage
import top.nipuru.prushka.common.message.auth.PlayerMessage
import top.nipuru.prushka.common.message.database.PlayerDataRequestMessage
import top.nipuru.prushka.game.route.authRequest
import top.nipuru.prushka.game.route.databaseRequest
import java.net.InetAddress
import java.util.*

object DataReader {
    fun read(name: String, uniqueId: UUID, address: InetAddress): GamePlayer {
        // 优先从别的服务器转移数据
        val transferRequest = PlayerDataTransferMessage(uniqueId)
        val playerData = Broker.invokeSync<PlayerDataMessage>(transferRequest)

        if (playerData != null) {
            val dataInfo = DataInfo(playerData.data)
            val gamePlayer = GamePlayer(playerData.playerId, playerData.dbId, name, uniqueId)
            gamePlayer.unpack(dataInfo)
            return gamePlayer
        } else {
            // 其他服务器没有数据代表登录 需要新建数据或从数据库加载
            val playerRequest = PlayerRequestMessage(name, uniqueId, address.hostAddress)
            val player = authRequest<PlayerMessage>(playerRequest)!!
            val playerDataRequest = PlayerDataRequestMessage(player.playerId)
            val gamePlayer = GamePlayer(player.playerId, player.dbId, name, uniqueId)
            gamePlayer.preload(playerDataRequest)
            val dataInfo = DataInfo(databaseRequest(player.dbId, playerDataRequest)!!)
            gamePlayer.unpack(dataInfo)
            return gamePlayer
        }
    }
}
