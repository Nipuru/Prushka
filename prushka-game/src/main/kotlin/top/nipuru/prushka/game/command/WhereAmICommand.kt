package top.nipuru.prushka.game.command

import net.afyer.afybroker.client.Broker
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


/**
 * 查看自己所在的服务器和世界信息
 * Cmd: /whereami
 *
 * @author Nipuru
 * @since 2024/11/13 15:25
 */
class WhereAmICommand : AbstractCommand("whereami") {
    override fun hasPermission(sender: CommandSender): Boolean = true

    override fun canConsoleExecute(): Boolean = true

    override fun onCommand(sender: CommandSender, args: Array<String>) {
        val serverName = Broker.getClientInfo().name
        sender.sendMessage(
            text("你位于服务器: ").color(NamedTextColor.BLUE)
                .append(text(serverName).color(NamedTextColor.WHITE))
        )
        if (sender is Player) {
            val worldName: String = sender.world.name
            sender.sendMessage(
                text("世界: ").color(NamedTextColor.BLUE)
                    .append(text(worldName).color(NamedTextColor.WHITE))
            )
        }
    }

}