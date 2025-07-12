package server.bukkit.gameplay.chat.formatter

import net.kyori.adventure.text.Component
import server.bukkit.gameplay.chat.Fragment
import server.bukkit.gameplay.player.GamePlayer
import server.common.message.PlayerInfoMessage
import java.util.regex.Pattern


/**
 * @author Nipuru
 * @since 2024/11/13 09:35
 */
abstract class SimpleMessageFormatter(pattern: Pattern) : MessagePattern(pattern) {
    final override fun parse(player: GamePlayer, vararg args: String?): Fragment {
        return Fragment(*args)
    }

    @Suppress("UNCHECKED_CAST")
    final override fun format(sender: PlayerInfoMessage, receiver: GamePlayer, fragment: Fragment): Component {
        return format(sender, receiver, fragment.args as Array<String?>)
    }

    abstract fun format(sender: PlayerInfoMessage, receiver: GamePlayer, args: Array<String?>): Component
}
