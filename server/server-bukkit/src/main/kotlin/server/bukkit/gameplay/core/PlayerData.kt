package server.bukkit.gameplay.core

import server.bukkit.gameplay.player.Data
import server.bukkit.gameplay.player.Table

@Table(name = "tb_player")
class PlayerData : Data {
    /** 货币  */
    var coin: Long = 0

    /** 点券  */
    var points: Long = 0

    /** 头衔id  */
    var rankId: Int = 0

    /** 创建时间  */
    var createTime: Long = 0

    /** 最后离线时间  */
    var logoutTime: Long = 0

    /** 重置时间  */
    var resetTime: Long = 0

    /** 累计在线时间  */
    var playedTime: Long = 0

    /** 生日 birthday[0]:月,birthday[1]:日  */
    var birthday: IntArray = IntArray(0)
}
