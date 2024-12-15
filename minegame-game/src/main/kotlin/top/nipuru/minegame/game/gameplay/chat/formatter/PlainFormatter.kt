package top.nipuru.minegame.game.gameplay.chat.formatter

import top.nipuru.minegame.common.message.shared.PlayerInfoMessage
import top.nipuru.minegame.game.gameplay.chat.Fragment
import top.nipuru.minegame.game.gameplay.player.GamePlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class PlainFormatter : MessageFormatter {

    override fun parse(player: GamePlayer, vararg args: String?): Fragment? {
        val rawMessage = args[0]
        if (rawMessage.isNullOrEmpty()) return null
        return Fragment(rawMessage)
    }

    override fun format(sender: PlayerInfoMessage, receiver: GamePlayer, fragment: Fragment): Component {
        val builder = Component.text()
        val rawMessage = fragment.getArg<String>(0)
        if (sender.name != receiver.name) {
            val split = receiver.namePattern.split(rawMessage, -1)
            for (i in split.indices) {
                builder.append(Component.text(split[i]))
                if (i < split.size - 1) {
                    builder.append(Component.text(receiver.name).color(NamedTextColor.GOLD))
                }
            }
        } else {
            builder.content(rawMessage)
        }
        return builder.build()
    }
}
