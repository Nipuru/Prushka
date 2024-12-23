package top.nipuru.prushka.game.gameplay.offline

import net.afyer.afybroker.client.Broker
import top.nipuru.prushka.common.message.PlayerOfflineDataMessage
import top.nipuru.prushka.common.message.database.QueryPlayerRequest
import top.nipuru.prushka.game.gameplay.player.BaseManager
import top.nipuru.prushka.game.gameplay.player.DataInfo
import top.nipuru.prushka.game.gameplay.player.GamePlayer
import top.nipuru.prushka.game.gameplay.player.preload
import top.nipuru.prushka.game.util.submit
import java.util.*

class OfflineManager(player: GamePlayer) : BaseManager(player) {
    private val offlineDataHandlers = mutableMapOf<String, OfflineDataHandler>()
    private val offlineDataList = mutableListOf<OfflineData>()
    private val offlineDataMessageQueue = LinkedList<PlayerOfflineDataMessage>()

    fun preload(request: QueryPlayerRequest) {
        request.preload(OfflineData::class.java)
    }

    fun unpack(dataInfo: DataInfo) {
        dataInfo.unpackList(OfflineData::class.java).forEach(offlineDataList::add)
    }

    fun pack(dataInfo: DataInfo) {
        offlineDataList.forEach(dataInfo::pack)
    }

    fun tick() {
        this.pushOfflineData()
    }

    fun getHandler(module: String): OfflineDataHandler? {
        return offlineDataHandlers[module]
    }

    fun registerHandler(module: String, handler: OfflineDataHandler) {
        require(!offlineDataHandlers.containsKey(module)) { "Offline handler for module: $module has benn registered" }
        offlineDataHandlers[module] = handler
    }

    fun pushOfflineData(name: String, playerId: Int, dbId: Int, moduleName: String, data: String) {
        val message = PlayerOfflineDataMessage(name, playerId, dbId, moduleName, data)
        // 并不立即发送，而是等到下一tick
        // 为什么这么做呢，因为如果调用此方法的时候，玩家已经离线了，就没必要发送了
        offlineDataMessageQueue.add(message)
    }

    fun onJoin() {
        offlineDataList.removeIf {
            val offlineDataHandler = offlineDataHandlers[it.module] ?: return@removeIf false
            if (offlineDataHandler.handle(it.data, false)) {
                player.delete(it)
                return@removeIf true
            }
            false
        }
    }

    private fun pushOfflineData() {
        if (offlineDataMessageQueue.isEmpty()) return
        val messages = offlineDataMessageQueue.toList()
        offlineDataMessageQueue.clear()
        submit { messages.forEach(Broker::oneway) }
    }
}
