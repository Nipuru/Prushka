package server.bukkit.gameplay.blacklist

import server.bukkit.gameplay.player.Table
import server.bukkit.gameplay.player.Unique


/**
 * 黑名单 屏蔽的玩家
 *
 * @author Nipuru
 * @since 2025/06/10 16:41
 */
@Table(name = "tb_blacklist_outbound")
class BlacklistOutboundData {
    @Unique
    var blockedId: Int = 0

    var createTime: Long = 0
}