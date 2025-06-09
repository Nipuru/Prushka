package server.bukkit.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.gameplay.player.GamePlayers
import server.common.message.TeleportType


/**
 * 强制传送玩家
 * Cmd: /prushka tpa <玩家名>
 * Cmd: /prushka tpahere <玩家名>
 *
 * @author Nipuru
 * @since 2024/11/28 17:24
 */
abstract class PrushkaTeleportAbstractCommand(name: String) : AbstractCommand(name) {
    override fun canConsoleExecute() = false
    override fun onCommand(sender: CommandSender, args: Array<String>) {
        sender as Player
        sendUsageIf(sender, "/prushka $name <玩家名>") { args.size != 1 }
        val senderPlayer = GamePlayers.getPlayer(sender.uniqueId)
        async(sender) {
            val receiver = getPlayerInfo(sender, args[0])
            handle(senderPlayer, receiver.name)
        }
    }
    override fun onTabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            // TODO 全服玩家名
            1 -> Bukkit.getOnlinePlayers()
                .map { player -> player.name }
                .filter { it.contains(args[0], true) }
            else -> emptyList()
        }
    }
    abstract fun handle(sender: GamePlayer, receiver: String)
}

class PrushkaTeleportAtCommand : PrushkaTeleportAbstractCommand("tpa") {
    override fun handle(sender: GamePlayer, receiver: String) {
        sender.teleport.teleport(receiver, TeleportType.TPA)
    }
}

class PrushkaTeleportAtHereCommand : PrushkaTeleportAbstractCommand("tpahere") {
    override fun handle(sender: GamePlayer, receiver: String) {
        sender.teleport.teleport(receiver, TeleportType.TPAHERE)
    }
}