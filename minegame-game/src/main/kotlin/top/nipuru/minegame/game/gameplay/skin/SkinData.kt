package top.nipuru.minegame.game.gameplay.skin

import top.nipuru.minegame.game.gameplay.player.Data
import top.nipuru.minegame.game.gameplay.player.Table


/**
 * @author Nipuru
 * @since 2024/11/29 15:07
 */
@Table(name = "tb_skin")
class SkinData : Data {

    /** 皮肤材质 长度2(value|signature) */
    var texture: Array<String> = emptyArray()
        internal set
}
