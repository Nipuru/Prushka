package top.nipuru.prushka.game.gameplay.teleport

import top.nipuru.prushka.game.gameplay.player.Data
import top.nipuru.prushka.game.gameplay.player.Table


/**
 * 传送历史记录
 *
 * @author Nipuru
 * @since 2024/11/20 17:17
 */
@Table(name = "tb_teleport_history")
class TeleportHistory : Data {
    /** 目标玩家 */
    var player: String = ""

    /** 是否是自己发送的 */
    val isSender: Boolean = false
}
