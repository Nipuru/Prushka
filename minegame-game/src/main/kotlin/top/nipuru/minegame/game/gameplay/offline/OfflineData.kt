package top.nipuru.minegame.game.gameplay.offline

import top.nipuru.minegame.game.gameplay.player.Data
import top.nipuru.minegame.game.gameplay.player.Table
import top.nipuru.minegame.game.gameplay.player.Unique

/**
 * 离线消息是一种特殊玩家的数据，使用单独的方法新增，可以在玩家离线时新增
 * 只能在玩上线后处理并且删除
 */
@Table(name = "tb_offline", autoCreate = false)
class OfflineData : Data {
    /** 每条离线消息都有自己的 id  */
    @Unique
    var id: Long = 0

    /** 所属模块  */
    var module: String = ""

    /** 数据，很可能是json  */
    var data: String = ""
}
