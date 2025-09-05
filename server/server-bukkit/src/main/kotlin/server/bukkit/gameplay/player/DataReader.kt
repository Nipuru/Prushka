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
        // 优先从别的服务器转移 如果不存在则读数据库
        return transferData(uniqueId, name) ?: queryData(uniqueId, name, address)
    }

    private fun transferData(uniqueId: UUID, name: String): GamePlayer? {
        // 从别的服务器转移数据
        val transferRequest = PlayerDataTransferRequest(uniqueId)
        val (playerId, dbId, data) = Broker.invokeSync<PlayerDataMessage>(transferRequest) ?: return null
        val dataInfo = DataInfo(data)
        val player = GamePlayer(playerId, dbId, name, uniqueId)
        player.unpack(dataInfo)
        return player
    }

    private fun queryData(uniqueId: UUID, name: String, address: InetAddress): GamePlayer {
        // 新建数据或从数据库加载
        val (playerId, dnId) = playerService.load(name, uniqueId, address.hostAddress)
        val player = GamePlayer(playerId, dnId, name, uniqueId)
        val tableInfos = TableInfos()
        player.preload(tableInfos)
        val data = player.dataService.queryPlayer(playerId, tableInfos.tables)
        player.unpack(DataInfo(data))
        return player
    }
}
