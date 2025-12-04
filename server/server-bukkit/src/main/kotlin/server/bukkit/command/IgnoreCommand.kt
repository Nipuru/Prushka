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
 * @since 2025/12/04 20:47
 */
class IgnoreCommand : CommandTree {
    override val root: LiteralCommandNode<CommandSourceStack> = literal("ignore")
        .then(argument("player_name", PlayerInfoArgument)
            .executesAsync(::ignore))
        .build()

    /**
     * 屏蔽/取消屏蔽 指定玩家
     * /ignore <player_name>
     */
    private fun ignore(context: CommandContext<CommandSourceStack>): Int {
        val target = context.getArgument<PlayerInfoMessage?>("player_name")
        val sender = context.source.gamePlayer
        if (target == null) {
            MessageType.FAILED.sendMessage(sender, "玩家不存在")
            return Command.SINGLE_SUCCESS
        }
        if (sender.blacklist.isBlocking(target.playerId)) {
            sender.blacklist.remove(target.name, target.playerId, target.dbId)
            MessageType.ALLOW.sendMessage(sender, "已取消屏蔽玩家 ${target.name}")
        } else {
            sender.blacklist.add(target.name, target.playerId, target.dbId)
            MessageType.ALLOW.sendMessage(sender, "已屏蔽玩家 ${target.name}")
        }
        return Command.SINGLE_SUCCESS
    }
}
