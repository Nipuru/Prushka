package top.nipuru.minegame.game.gameplay.chat.formatter

import top.nipuru.minegame.common.message.shared.PlayerInfoMessage
import top.nipuru.minegame.game.gameplay.chat.Fragment
import top.nipuru.minegame.game.gameplay.player.GamePlayer
import net.kyori.adventure.text.Component

interface MessageFormatter {
    fun parse(player: GamePlayer, vararg args: String?): Fragment?
    fun format(sender: PlayerInfoMessage, receiver: GamePlayer, fragment: Fragment): Component
}
