package top.nipuru.minegame.game.gameplay.friend

import top.nipuru.minegame.game.gameplay.player.Data
import top.nipuru.minegame.game.gameplay.player.Table
import top.nipuru.minegame.game.gameplay.player.Unique

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
