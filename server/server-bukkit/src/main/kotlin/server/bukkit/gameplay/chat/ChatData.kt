package server.bukkit.gameplay.chat

import server.bukkit.gameplay.player.Data
import server.bukkit.gameplay.player.Table

@Table(name = "tb_chat")
class ChatData : Data {
    /** 禁言截止时间  */
    var mute: Long = 0L

    /** 私聊玩家名  */
    var msgTarget: String = ""
}
