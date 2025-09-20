package server.bukkit.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands.literal
import net.afyer.afybroker.client.Broker
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
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
        return Command.SINGLE_SUCCESS
    }


}