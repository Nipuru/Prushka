package server.bukkit.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands.argument
import io.papermc.paper.command.brigadier.Commands.literal
import server.bukkit.MessageType
import server.bukkit.command.argument.PlayerInfoArgument
import server.bukkit.util.CommandTree
import server.common.message.PlayerInfoMessage

/**
 * @author Nipuru
 * @since 2025/09/20 11:59
 */
class MsgModeCommand : CommandTree {
    override val root: LiteralCommandNode<CommandSourceStack> = literal("msgmode")
        .then(argument("player_name", PlayerInfoArgument)
            .executesAsync(::enter))
        .executes(::quit)
        .build()

    /**
     * 进入私聊模式
     * /msgmode <player_name>
     */
    private fun enter(context: CommandContext<CommandSourceStack>): Int {
        val target = context.getArgument<PlayerInfoMessage?>("player_name")
        val sender = context.source.gamePlayer
        if (target == null) {
            MessageType.FAILED.sendMessage(sender, "玩家不存在")
            return Command.SINGLE_SUCCESS
        }
        if (sender.chat.msgTarget == target.name) {
            MessageType.FAILED.sendMessage(sender, "你已经与 ${target.name} 进入私聊模式")
            return Command.SINGLE_SUCCESS
        }
        sender.chat.msgTarget = target.name
        MessageType.ALLOW.sendMessage(sender, "已与 ${target.name} 进入私聊模式")
        return Command.SINGLE_SUCCESS
    }

    /**
     * 退出私聊模式
     * /msgmode
     */
    private fun quit(context: CommandContext<CommandSourceStack>): Int {
        val sender = context.source.gamePlayer
        if (sender.chat.msgTarget == "") {
            MessageType.FAILED.sendMessage(sender, "你当前没有进入私聊模式")
            MessageType.INFO.sendMessage(sender, "输入 /msgmode <玩家> 进入私聊模式")
            return Command.SINGLE_SUCCESS
        }
        sender.chat.msgTarget = ""
        MessageType.ALLOW.sendMessage(sender, "已退出私聊模式")
        return Command.SINGLE_SUCCESS
    }

}