package server.bukkit.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands.literal
import net.afyer.afybroker.client.Broker
import org.bukkit.entity.Player
import server.bukkit.MessageType
import server.bukkit.util.CommandTree


/**
 * 查看自己所在的服务器和世界信息
 * Cmd: /whereami
 *
 * @author Nipuru
 * @since 2024/11/13 15:25
 */
class WhereAmICommand : CommandTree {

    override val root: LiteralCommandNode<CommandSourceStack> = literal("whereami")
        .executes(::whereami)
        .build()

    private fun whereami(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.source.sender
        val serverName = Broker.getClientInfo().name
        var message = "你位于服务器: <white>$serverName</white>"
        if (sender is Player) {
            val worldName: String = sender.world.name
            message += ", 世界: <white>$worldName</white>"
        }
        MessageType.INFO.sendMessage(sender, message)
        return Command.SINGLE_SUCCESS
    }


}