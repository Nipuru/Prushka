package server.bukkit.gameplay.item

import server.bukkit.constant.Items
import server.bukkit.gameplay.player.*
import server.bukkit.logger.LogServer
import server.common.logger.logger


class ItemManager(player: GamePlayer) : BaseManager(player) {
    
    private val items = mutableMapOf<Pair<Int, Int>, ItemData>()

    fun preload(request: TableInfos) {
        request.preload<ItemData>()
    }
    
    fun unpack(dataInfo: DataInfo) {
        for (item in dataInfo.unpackList<ItemData>()) {
            items[item.type to item.id] = item
        }
    }
    
    fun pack(dataInfo: DataInfo) {
        for (item in items.values) {
            dataInfo.pack(item)
        }
    }

    fun giveRewards(rewards: Array<RewardInfo>, way: Int) {
        for (reward in rewards) {
            when (reward.type) {
                Items.ITEM_PROPERTY -> addProperty(reward.id, reward.num.toLong(), way)
                else -> addItem(reward.type, reward.id, reward.num.toLong(), way)
            }
        }
    }

    fun getPropertyAmount(id: Int): Long {
        return when (id) {
            Items.PROPERTY_COIN -> player.core.coin
            Items.PROPERTY_POINTS -> player.core.points
            else -> getItemAmount(Items.ITEM_PROPERTY, id)
        }
    }

    fun addProperty(id: Int, amount: Long, way: Int): Boolean {
        return when (id) {
            Items.PROPERTY_COIN -> player.core.addCoin(amount, way)
            Items.PROPERTY_POINTS -> player.core.addPoints(amount, way)
            else -> addItem(Items.ITEM_PROPERTY, id, amount, way)
        }
    }

    fun subtractProperty(id: Int, amount: Long, way: Int): Boolean {
        return when (id) {
            Items.PROPERTY_COIN -> player.core.subtractCoin(amount, way)
            Items.PROPERTY_POINTS -> player.core.subtractPoints(amount, way)
            else -> subtractItem(Items.ITEM_PROPERTY, id, amount, way)
        }
    }

    fun checkProperties(properties: Map<Int, Int>): Boolean {
        return properties.all { (id, needAmount) ->
            needAmount == 0 || needAmount > 0 && getPropertyAmount(id) >= needAmount
        }
    }

    fun subtractProperties(properties: Map<Int, Int>, way: Int) {
        properties.forEach { (id, amount) ->
            subtractProperty(id, amount.toLong(), way)
        }
    }

    private fun getItemAmount(type: Int, id: Int): Long {
        return getItem(type, id).amount
    }

    private fun addItem(type: Int, id: Int, amount: Long, way: Int): Boolean {
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

    private fun subtractItem(type: Int, id: Int, amount: Long, way: Int): Boolean {
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
        return items.getOrPut(type to id) {
            ItemData().apply {
                this.type = type
                this.id = id
                player.insert(this)
            }
        }
    }
}
