package top.nipuru.prushka.game.gameplay.inventory

import top.nipuru.prushka.common.message.database.PlayerDataRequestMessage
import top.nipuru.prushka.game.gameplay.player.BaseManager
import top.nipuru.prushka.game.gameplay.player.DataInfo
import top.nipuru.prushka.game.gameplay.player.GamePlayer
import top.nipuru.prushka.game.gameplay.player.preload
import top.nipuru.prushka.game.logger.logger
import top.nipuru.prushka.game.nms.*
import org.bukkit.GameMode
import org.bukkit.entity.Player

class InventoryManager(player: GamePlayer) : BaseManager(player) {
    private var data: InventoryData? = null

    fun preload(request: PlayerDataRequestMessage) {
        request.preload(InventoryData::class.java)
    }

    fun unpack(dataInfo: DataInfo) {
        data = dataInfo.unpack(InventoryData::class.java) ?: return
    }

    fun pack(dataInfo: DataInfo) {
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
        bukkitPlayer.inventory.setArmorContents(null)
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
        bukkitPlayer.persistentDataContainer.clear()
    }

    private fun applyPlayer(bukkitPlayer: Player, data: InventoryData) {
        if (data.inventory.isNotEmpty()) {
            bukkitPlayer.inventory.contents = data.inventory.deserializeItemStacks()
        }

        bukkitPlayer.inventory.heldItemSlot = data.hotBar
        bukkitPlayer.gameMode = GameMode.values()[data.gameMode]
        if (data.enderChest.isNotEmpty()) {
            bukkitPlayer.enderChest.contents = data.enderChest.deserializeItemStacks()
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
            bukkitPlayer.persistentDataContainer.deserialize(data.bukkitValues)
        }
        bukkitPlayer.fireTicks = data.fireTicks
        bukkitPlayer.freezeTicks = data.freezeTicks
        bukkitPlayer.health = data.health
        bukkitPlayer.healthScale = data.healthScale
        bukkitPlayer.isHealthScaled = data.healthScaled
        logger.info("InventoryData has applied for GamePlayer: {}", player.name)
    }

    private fun savePlayer(bukkitPlayer: Player, data: InventoryData) {
        data.inventory = bukkitPlayer.inventory.contents.serialize()
        data.hotBar = bukkitPlayer.inventory.heldItemSlot
        data.gameMode = bukkitPlayer.gameMode.ordinal
        data.enderChest = bukkitPlayer.enderChest.contents.serialize()
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
        data.bukkitValues = bukkitPlayer.persistentDataContainer.serialize()
        data.fireTicks = bukkitPlayer.fireTicks
        data.freezeTicks = bukkitPlayer.freezeTicks
        logger.info("Saving InventoryData for GamePlayer: {}", player.name)
    }
}
