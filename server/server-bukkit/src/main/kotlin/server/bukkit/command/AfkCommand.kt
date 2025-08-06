package server.bukkit.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands


/**
 * 进入挂机模式
 * Cmd: /afk
 *
 * @author Nipuru
 * @since 2024/11/12 18:02
 */
@Suppress("UnstableApiUsage")
object AfkCommand {
    fun register(registrar: Commands) {
        registrar.register(Commands.literal("afk")
            .requires(CommandSourceStack::isPlayer)
            .executes(::afk)
            .build())
    }

    private fun afk(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.sender.player
        player.core.afk = true
        return Command.SINGLE_SUCCESS
    }
}