package server.bukkit.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import server.bukkit.gameplay.player.GamePlayers


/**
 * 进入挂机模式
 * Cmd: /afk
 *
 * @author Nipuru
 * @since 2024/11/12 18:02
 */
class AfkCommand : AbstractCommand("afk") {
    override fun canConsoleExecute() = false
    override fun onCommand(sender: CommandSender, args: Array<String>) {
        sender as Player
        val player = GamePlayers.getPlayer(sender.uniqueId)
        player.core.afk = true
    }
}