package server.bukkit.command

import org.bukkit.command.CommandSender


/**
 * 管理员命令根节点
 * Cmd: /prushka
 *
 * @author Nipuru
 * @since 2024/11/19 15:11
 */
class PrushkaCommand : AbstractCommand("prushka") {
    override fun hasPermission(sender: CommandSender) = sender.isOp

    init {
        subCommand(PrushkaWorldCommand())
        subCommand(PrushkaTextCommand())
        subCommand(PrushkaReloadCommand())
        subCommand(PrushkaTeleportAtCommand())
        subCommand(PrushkaTeleportAtHereCommand())
    }
}

