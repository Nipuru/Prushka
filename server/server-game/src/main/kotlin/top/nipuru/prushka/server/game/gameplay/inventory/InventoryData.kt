package top.nipuru.prushka.server.game.gameplay.inventory

import top.nipuru.prushka.server.game.gameplay.player.Data
import top.nipuru.prushka.server.game.gameplay.player.Table

@Table(name = "tb_inventory")
class InventoryData : Data {
    /** 玩家背包  */
    var inventory: ByteArray = ByteArray(0)

    /** 玩家快捷栏选中的格子序号  */
    var hotBar: Int = 0

    /** 玩家的游戏模式  */
    var gameMode: Int = 0

    /** 末影箱  */
    var enderChest: ByteArray = ByteArray(0)

    /** 经验条进度  */
    var experience: Float = 0f

    /** 总经验值  */
    var totalExperience: Int = 0

    /** 经验等级  */
    var experienceLevel: Int = 0

    /** 药水效果  */
    var potionEffects: ByteArray = ByteArray(0)

    /** 生命值  */
    var health: Double = 0.0

    /** 生命缩放  */
    var healthScale: Double = 0.0

    /** 生命是否缩放  */
    var healthScaled: Boolean = false

    /** 饥饿值  */
    var foodLevel: Int = 0

    /** 饱和度  */
    var saturation: Float = 0f

    /** 空气条  */
    var air: Int = 0

    /** 最大空气条  */
    var maxAir: Int = 0

    /** PersistentData 数据  */
    var bukkitValues: ByteArray = ByteArray(0)

    /** 着火游戏刻  */
    var fireTicks: Int = 0

    /** 冰冻游戏刻  */
    var freezeTicks: Int = 0
}
