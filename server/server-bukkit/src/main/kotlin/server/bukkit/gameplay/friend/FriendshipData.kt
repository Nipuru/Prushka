package server.bukkit.gameplay.friend

import server.bukkit.gameplay.player.Data
import server.bukkit.gameplay.player.Table
import server.bukkit.gameplay.player.Unique

@Table(name = "tb_friendship")
class FriendshipData : Data {
    /** 好友 id  */
    @Unique
    var friendId: Int = 0

    /** 建立时间  */
    var createTime: Long = 0

    /** 备注信息  */
    var remark: String = ""
}
