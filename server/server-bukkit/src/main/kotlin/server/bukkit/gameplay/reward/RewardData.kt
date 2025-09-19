package server.bukkit.gameplay.reward

import server.bukkit.gameplay.player.Data
import server.bukkit.gameplay.player.Table
import server.bukkit.gameplay.player.Unique

@Table(name = "tb_reward")
class RewardData(
    /** 物品类型  */
    @Unique
    var type: Int = 0,

    /** 物品id  */
    @Unique
    var id: Int = 0,

    /** 数量  */
    var amount: Long = 0L
) : Data
