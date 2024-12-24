package top.nipuru.prushka.game.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.nipuru.prushka.common.message.TeleportType
import top.nipuru.prushka.game.gameplay.player.GamePlayer
import top.nipuru.prushka.game.gameplay.player.GamePlayers


/**
 * 玩家传送
 * Cmd: /tpa <player_name>
 * Cmd: /tpahere <player_name>
 * Cmd: /tpaccept <player_name>
 * Cmd: /tpdeny <player_name>
 *
 * @author Nipuru
 * @since 2024/11/20 13:43
 */
abstract class TeleportAbstractCommand(name: String) : AbstractCommand(name) {
    override fun canConsoleExecute() = false
    override fun onCommand(sender: CommandSender, args: Array<String>) {
        sender as Player
        sendUsageIf(sender, "/$name <玩家名>") { args.size != 1 }
        val senderPlayer = GamePlayers.getPlayer(sender.uniqueId)
        handle(senderPlayer, args.first())
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

class TeleportAtCommand : TeleportAbstractCommand("tpa") {
    override fun handle(sender: GamePlayer, receiver: String) {
        sender.teleport.teleportRequest(receiver, TeleportType.TPA)
    }
}

class TeleportAtHereCommand : TeleportAbstractCommand("tpahere") {
    override fun handle(sender: GamePlayer, receiver: String) {
        sender.teleport.teleportRequest(receiver, TeleportType.TPAHERE)
    }
}

class TeleportAcceptCommand : TeleportAbstractCommand("tpaccept") {
    override fun handle(sender: GamePlayer, receiver: String) {
        sender.teleport.teleportResponse(receiver, true)
    }
}

class TeleportDenyCommand : TeleportAbstractCommand("tpdeny") {
    override fun handle(sender: GamePlayer, receiver: String) {
        sender.teleport.teleportResponse(receiver, false)
    }
}

