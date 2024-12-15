package top.nipuru.minegame.game.command

import org.bukkit.command.CommandSender


/**
 * 管理员命令根节点
 * Cmd: /mg
 *
 * @author Nipuru
 * @since 2024/11/19 15:11
 */
class MGCommand : AbstractCommand("minegame", "mg") {
    override fun hasPermission(sender: CommandSender) = sender.isOp

    init {
        subCommand(MGWorldCommand())
        subCommand(MGTextCommand())
        subCommand(MGReloadCommand())
    }
}

