package server.bukkit.gameplay.blacklist

import server.bukkit.gameplay.player.Data
import server.bukkit.gameplay.player.Table
import server.bukkit.gameplay.player.Unique


/**
 * 黑名单 被哪些玩家屏蔽
 *
 * @author Nipuru
 * @since 2025/06/10 16:41
 */
@Table(name = "tb_blacklist_inbound")
class BlacklistInboundData : Data {
    /** 玩家 id  */
    @Unique
    var blockerId: Int = 0
}