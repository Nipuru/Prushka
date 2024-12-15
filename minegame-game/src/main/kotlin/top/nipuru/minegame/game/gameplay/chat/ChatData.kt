package top.nipuru.minegame.game.gameplay.chat

import top.nipuru.minegame.game.gameplay.player.Data
import top.nipuru.minegame.game.gameplay.player.Table

@Table(name = "tb_chat")
class ChatData : Data {
    /** 禁言截止时间  */
    var mute: Long = 0L

    /** 私聊玩家名  */
    var msgTarget: String = ""
}
