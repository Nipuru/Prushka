package server.bukkit.gameplay.blacklist

import server.bukkit.gameplay.player.Data
import server.bukkit.gameplay.player.Table
import server.bukkit.gameplay.player.Unique


/**
 * 黑名单
 *
 * @author Nipuru
 * @since 2025/06/10 16:41
 */
@Table(name = "tb_blacklist")
class BlacklistData : Data {
    /** 玩家 id  */
    @Unique
    var target: Int = 0
     
    /** 屏蔽状态 */
    var blocking: Boolean = false
    
    /** 被屏蔽状态 */
    var blocked: Boolean = false
}