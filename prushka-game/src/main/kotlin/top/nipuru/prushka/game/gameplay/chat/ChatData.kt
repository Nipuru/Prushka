package top.nipuru.prushka.game.gameplay.chat

import top.nipuru.prushka.game.gameplay.player.Data
import top.nipuru.prushka.game.gameplay.player.Table

@Table(name = "tb_chat")
class ChatData : Data {
    /** 禁言截止时间  */
    var mute: Long = 0L

    /** 私聊玩家名  */
    var msgTarget: String = ""
}
