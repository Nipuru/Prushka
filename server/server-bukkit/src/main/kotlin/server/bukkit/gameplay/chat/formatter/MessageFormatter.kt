package server.bukkit.gameplay.chat.formatter

import net.kyori.adventure.text.Component
import server.bukkit.gameplay.chat.Fragment
import server.bukkit.gameplay.player.GamePlayer
import server.common.message.shared.PlayerInfoMessage

interface MessageFormatter {
    fun parse(player: GamePlayer, vararg args: String?): Fragment?
    fun format(sender: PlayerInfoMessage, receiver: GamePlayer, fragment: Fragment): Component
}
