package top.nipuru.prushka.server.game.gameplay.chat

import top.nipuru.prushka.server.game.gameplay.player.Data
import top.nipuru.prushka.server.game.gameplay.player.Table

@Table(name = "tb_chat")
class ChatData : Data {
    /** 禁言截止时间  */
    var mute: Long = 0L

    /** 私聊玩家名  */
    var msgTarget: String = ""
}
