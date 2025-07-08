package server.bukkit.gameplay.item

import server.bukkit.gameplay.player.BaseManager
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.gameplay.player.preload
import server.bukkit.logger.LogServer
import server.common.logger.logger
import server.common.message.database.PlayerDataQueryRequest


class ItemManager(player: GamePlayer) : BaseManager(player) {
    
    private val items = mutableMapOf<Int, MutableMap<Int, ItemData>>()

    fun preload(request: PlayerDataQueryRequest) {
        request.preload<ItemData>()
    }
    
    fun unpack(dataInfo: server.bukkit.gameplay.player.DataInfo) {
        for (item in dataInfo.unpackList<ItemData>()) {
            val byId = items.getOrPut(item.type) { mutableMapOf() }
            byId[item.id] = item
        }
    }
    
    fun pack(dataInfo: server.bukkit.gameplay.player.DataInfo) {
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
