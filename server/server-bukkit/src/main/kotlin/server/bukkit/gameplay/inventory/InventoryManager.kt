package server.bukkit.gameplay.inventory

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import server.bukkit.gameplay.player.BaseManager
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.gameplay.player.TableInfos
import server.bukkit.gameplay.player.preload
import server.common.logger.logger
import server.bukkit.nms.*

class InventoryManager(player: GamePlayer) : BaseManager(player) {
    private var data: InventoryData? = null

    fun preload(request: TableInfos) {
        request.preload<InventoryData>()
    }

    fun unpack(dataInfo: server.bukkit.gameplay.player.DataInfo) {
        data = dataInfo.unpack<InventoryData>() ?: return
    }

    fun pack(dataInfo: server.bukkit.gameplay.player.DataInfo) {
        val data = this.data
        if (data != null) {
            dataInfo.pack(data)
        }
    }

    fun onJoin() {
        // 清除玩家数据
        resetPlayer(player.bukkitPlayer)
        var data = this.data
        if (data == null) {
            data = InventoryData()
            savePlayer(player.bukkitPlayer, data)
            this.data = data
            player.insert(data)
            return
        }
        // 运用数据
        applyPlayer(player.bukkitPlayer, data)
    }

    fun onQuit() {
        // 将光标上的物品尽可能放回背包
        val itemOnCursor = player.bukkitPlayer.itemOnCursor
        if (!itemOnCursor.type.isAir) {
            player.bukkitPlayer.placeBackInInventory(itemOnCursor)
            player.bukkitPlayer.setItemOnCursor(null)
        }
        val data = this.data!!
        savePlayer(player.bukkitPlayer, data)
        player.update(data)
    }

    private fun resetPlayer(bukkitPlayer: Player) {
        bukkitPlayer.inventory.clear()
        bukkitPlayer.inventory.armorContents = emptyArray()
        bukkitPlayer.enderChest.clear()
        bukkitPlayer.exp = 0.0f
        bukkitPlayer.level = 0
        bukkitPlayer.foodLevel = 20
        bukkitPlayer.saturation = 5.0f
        bukkitPlayer.gameMode = GameMode.SURVIVAL
        bukkitPlayer.maximumAir = 300
        bukkitPlayer.remainingAir = 300
        for (potionEffect in bukkitPlayer.activePotionEffects) {
            bukkitPlayer.removePotionEffect(potionEffect.type)
        }
        bukkitPlayer.health = 20.0
        bukkitPlayer.healthScale = 20.0
        bukkitPlayer.persistentDataContainer.keys.forEach {
            bukkitPlayer.persistentDataContainer.remove(it)
        }
    }

    private fun applyPlayer(bukkitPlayer: Player, data: InventoryData) {
        if (data.inventory.isNotEmpty()) {
            bukkitPlayer.inventory.contents = ItemStack.deserializeItemsFromBytes(data.inventory)
        }

        bukkitPlayer.inventory.heldItemSlot = data.hotBar
        bukkitPlayer.gameMode = GameMode.entries[data.gameMode]
        if (data.enderChest.isNotEmpty()) {
            bukkitPlayer.enderChest.contents = ItemStack.deserializeItemsFromBytes(data.enderChest)
        }
        bukkitPlayer.exp = data.experience
        bukkitPlayer.totalExperience = data.totalExperience
        bukkitPlayer.level = data.experienceLevel

        if (data.potionEffects.isNotEmpty()) {
            for (potionEffect in data.potionEffects.deserializePotionEffects()) {
                bukkitPlayer.addPotionEffect(potionEffect)
            }
        }
        bukkitPlayer.foodLevel = data.foodLevel
        bukkitPlayer.saturation = data.saturation
        bukkitPlayer.remainingAir = data.air
        bukkitPlayer.maximumAir = data.maxAir
        if (data.bukkitValues.isNotEmpty()) {
            bukkitPlayer.persistentDataContainer.readFromBytes(data.bukkitValues)
        }
        bukkitPlayer.fireTicks = data.fireTicks
        bukkitPlayer.freezeTicks = data.freezeTicks
        bukkitPlayer.health = data.health
        bukkitPlayer.healthScale = data.healthScale
        bukkitPlayer.isHealthScaled = data.healthScaled
        logger.info("InventoryData has applied for GamePlayer: {}", player.name)
    }

    private fun savePlayer(bukkitPlayer: Player, data: InventoryData) {
        data.inventory = ItemStack.serializeItemsAsBytes(bukkitPlayer.inventory.contents)
        data.hotBar = bukkitPlayer.inventory.heldItemSlot
        data.gameMode = bukkitPlayer.gameMode.ordinal
        data.enderChest = ItemStack.serializeItemsAsBytes(bukkitPlayer.enderChest.contents)
        data.experience = bukkitPlayer.exp
        data.totalExperience = bukkitPlayer.totalExperience
        data.experienceLevel = bukkitPlayer.level
        data.potionEffects = bukkitPlayer.activePotionEffects.serialize()
        data.health = bukkitPlayer.health
        data.healthScale = bukkitPlayer.healthScale
        data.healthScaled = bukkitPlayer.isHealthScaled
        data.foodLevel = bukkitPlayer.foodLevel
        data.saturation = bukkitPlayer.saturation
        data.air = bukkitPlayer.remainingAir
        data.maxAir = bukkitPlayer.maximumAir
        data.bukkitValues = bukkitPlayer.persistentDataContainer.serializeToBytes()
        data.fireTicks = bukkitPlayer.fireTicks
        data.freezeTicks = bukkitPlayer.freezeTicks
        logger.info("Saving InventoryData for GamePlayer: {}", player.name)
    }
}
