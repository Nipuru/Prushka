package server.bukkit.gameplay.skin

import server.bukkit.gameplay.player.Data
import server.bukkit.gameplay.player.Table


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
