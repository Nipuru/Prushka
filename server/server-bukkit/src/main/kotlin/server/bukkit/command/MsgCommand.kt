package server.bukkit.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
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
@Suppress("UnstableApiUsage")
class MsgCommand : CommandTree {
    override val root: LiteralCommandNode<CommandSourceStack> = literal("msg")
        .then(argument("player_name", PlayerInfoArgument)
            .then(argument("message", StringArgumentType.greedyString())
                .executesAsync(::msg)))
        .build()

    /**
     * 发送私聊消息
     * /msg <player_name> <message>
     */
    private fun msg(context: CommandContext<CommandSourceStack>): Int {
        val target = context.getArgument<PlayerInfoMessage?>("player_name")
        val message = context.getArgument<String>("message")
        val sender = context.source.gamePlayer
        if (target == null) {
            MessageType.FAILED.sendMessage(sender, "玩家不存在")
            return Command.SINGLE_SUCCESS
        }
        if (sender.chat.isMuted) {
            MessageType.WARNING.sendMessage(sender, "你已被系统禁言.")
            return Command.SINGLE_SUCCESS
        }
        if (sender.chat.rateLimit()) {
            MessageType.WARNING.sendMessage(sender, "你的聊天频率过快, 请稍后再试.")
            return Command.SINGLE_SUCCESS
        }
        sender.chat.sendPrivateChat(target.name, message).thenApply { success ->
            if (!success) {
                MessageType.FAILED.sendMessage(sender, "消息发送失败, 私聊目标无法收到消息,")
            }
        }
        return Command.SINGLE_SUCCESS
    }

}