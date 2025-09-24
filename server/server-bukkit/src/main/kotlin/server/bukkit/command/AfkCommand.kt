package server.bukkit.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands.literal
import server.bukkit.MessageType
import server.bukkit.util.CommandTree


/**
 * 进入挂机模式
 * Cmd: /afk
 *
 * @author Nipuru
 * @since 2024/11/12 18:02
 */
class AfkCommand : CommandTree {

    override val root: LiteralCommandNode<CommandSourceStack> = literal("afk")
        .executes(::afk)
        .build()

    private fun afk(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        player.core.afk = true
        MessageType.ALLOW.sendMessage(player, "已进入挂机模式")
        return Command.SINGLE_SUCCESS
    }


}