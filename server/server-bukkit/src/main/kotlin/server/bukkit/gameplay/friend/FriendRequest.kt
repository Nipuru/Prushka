package server.bukkit.gameplay.friend

import server.bukkit.gameplay.player.Data
import server.bukkit.gameplay.player.Table
import server.bukkit.gameplay.player.Unique

@Table(name = "tb_friend_request")
class FriendRequest : Data {
    /** 玩家 id  */
    @Unique
    var friendId: Int = 0

    /** 建立时间  */
    var createTime: Long = 0
}
