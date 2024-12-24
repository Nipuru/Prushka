package top.nipuru.prushka.game.gameplay.teleport

import top.nipuru.prushka.game.gameplay.player.Data
import top.nipuru.prushka.game.gameplay.player.Table


/**
 * 传送请求 不存数据库
 *
 * @author Nipuru
 * @since 2024/11/20 17:17
 */
@Table(name = "cache_teleport_request", cache = true)
class TeleportRequestCache : Data {
    var sender: String = ""
    var type: Int = 0
    var expireAt: Long = 0
}
