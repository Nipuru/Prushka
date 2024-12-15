package top.nipuru.minegame.game.gameplay.friend

import top.nipuru.minegame.game.gameplay.player.Data
import top.nipuru.minegame.game.gameplay.player.Table
import top.nipuru.minegame.game.gameplay.player.Unique

@Table(name = "tb_friend_request")
class FriendRequest : Data {
    /** 玩家 id  */
    @Unique
    var friendId: Int = 0

    /** 建立时间  */
    var createTime: Long = 0
}
