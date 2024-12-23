package top.nipuru.prushka.game.gameplay.item

import top.nipuru.prushka.common.message.database.QueryPlayerRequest
import top.nipuru.prushka.game.gameplay.player.BaseManager
import top.nipuru.prushka.game.gameplay.player.DataInfo
import top.nipuru.prushka.game.gameplay.player.GamePlayer
import top.nipuru.prushka.game.gameplay.player.preload
import top.nipuru.prushka.game.logger.LogServer
import top.nipuru.prushka.game.logger.logger


class ItemManager(player: GamePlayer) : BaseManager(player) {
    
    private val items = mutableMapOf<Int, MutableMap<Int, ItemData>>()

    fun preload(request: QueryPlayerRequest) {
        request.preload(ItemData::class.java)
    }
    
    fun unpack(dataInfo: DataInfo) {
        for (item in dataInfo.unpackList(ItemData::class.java)) {
            val byId = items.getOrPut(item.type) { mutableMapOf() }
            byId[item.id] = item
        }
    }
    
    fun pack(dataInfo: DataInfo) {
        for (byId in items.values) {
            for (item in byId.values) {
                dataInfo.pack(item)
            }
        }
    }

    fun getItemAmount(type: Int, id: Int): Long {
        return getItem(type, id).amount
    }

    fun addItem(type: Int, id: Int, amount: Long, way: Int): Boolean {
        if (amount == 0L) return true
        if (amount < 0L) {
            logger.error("add invalid item amount: {}", amount)
            return false
        }
        val item = getItem(type, id)
        item.amount += amount
        LogServer.logAddItem(player.playerId, type, id, amount, way)
        player.update(item, ItemData::amount)
        return true
    }

    fun subtractItem(type: Int, id: Int, amount: Long, way: Int): Boolean {
        if (amount == 0L) return true
        if (amount < 0L) {
            logger.error("subtract invalid item amount: {}", amount)
            return false
        }
        val item = getItem(type, id)
        item.amount -= amount
        LogServer.logSubtractItem(player.playerId, type, id, amount, way)
        player.update(item, ItemData::amount)
        return true
    }

    private fun getItem(type: Int, id: Int): ItemData {
        return items.getOrPut(type) { mutableMapOf() }
            .getOrPut(id) {
                ItemData().apply {
                    this.type = type
                    this.id = id
                    player.insert(this)
                }
            }
    }
}
