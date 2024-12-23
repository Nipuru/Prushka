package top.nipuru.prushka.game.command

import top.nipuru.prushka.game.MessageType
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


/**
 * 将自己传送到当前服务器指定世界出生点
 * Cmd: /mg world <world_name>
 *
 * @author Nipuru
 * @since 2024/11/13 15:29
 */
internal class PrushkaWorldCommand : AbstractCommand("world") {
    override fun canConsoleExecute() = false
    override fun onCommand(sender: CommandSender, args: Array<String>) {
        sender as Player
        sendUsageIf(sender, "/world <世界名>") { args.isEmpty() }
        val worldName = args.joinToString(" ")
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            MessageType.FAILED.sendMessage(sender, "世界 $worldName 不存在")
            return
        }
        if (sender.world == world) {
            MessageType.FAILED.sendMessage(sender, "你已经位于世界 $worldName")
            return
        }
        sender.teleport(world.spawnLocation)
    }

    override fun onTabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return if (args.isNotEmpty()) {
            val worldName = args.joinToString(" ")
            Bukkit.getWorlds()
                .map { world -> world.name }
                .filter { it.startsWith(worldName) }
        } else emptyList()
    }
}