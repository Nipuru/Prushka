package top.nipuru.prushka.server.game.gameplay.item

import top.nipuru.prushka.server.game.gameplay.player.Data
import top.nipuru.prushka.server.game.gameplay.player.Table
import top.nipuru.prushka.server.game.gameplay.player.Unique

@Table(name = "tb_item")
class ItemData : Data {
    /** 物品类型  */
    @Unique
    var type: Int = 0

    /** 物品id  */
    @Unique
    var id: Int = 0

    /** 数量  */
    var amount: Long = 0L
}
