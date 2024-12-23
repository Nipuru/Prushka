package top.nipuru.prushka.game.command

import top.nipuru.prushka.game.gameplay.player.GamePlayers
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


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