package top.nipuru.prushka.game.gameplay.friend

import top.nipuru.prushka.game.gameplay.player.Data
import top.nipuru.prushka.game.gameplay.player.Table
import top.nipuru.prushka.game.gameplay.player.Unique

@Table(name = "tb_friend_request")
class FriendRequest : Data {
    /** 玩家 id  */
    @Unique
    var friendId: Int = 0

    /** 建立时间  */
    var createTime: Long = 0
}
