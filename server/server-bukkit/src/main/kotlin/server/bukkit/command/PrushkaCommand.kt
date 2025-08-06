package server.bukkit.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import server.bukkit.MessageType
import server.bukkit.command.argument.PlayerInfoArgument
import server.bukkit.command.argument.WorldArgument
import server.bukkit.plugin
import server.bukkit.util.component
import server.common.message.TeleportType


/**
 * 管理员命令根节点
 * Cmd: /prushka
 *
 * @author Nipuru
 * @since 2024/11/19 15:11
 */
@Suppress("UnstableApiUsage")
object PrushkaCommand {
    fun register(registrar: Commands) {
        registrar.register(Commands.literal("prushka")
            .requireOperator()
            .then(Commands.literal("text")
                .then(Commands.argument("args", StringArgumentType.greedyString()))
                .executes(::text))
            .then(Commands.literal("reload")
                .executes(::reload))
            .then(Commands.literal("world")
                .then(Commands.argument("world_name", WorldArgument)
                    .executes(::world)))
            .then(Commands.literal("tpa")
                .then(Commands.argument("player_name", PlayerInfoArgument)
                    .executes(::tpa)))
            .then(Commands.literal("tpahere")
                .then(Commands.argument("player_name", PlayerInfoArgument)
                    .executes(::tpahere)))
            .build())
    }

    private fun text(context: CommandContext<CommandSourceStack>): Int {
        val sender = context.source.sender
        val message = StringArgumentType.getString(context, "args").component()
        sender.sendMessage(message)
        return Command.SINGLE_SUCCESS
    }

    private fun reload(context: CommandContext<CommandSourceStack>): Int {
        val sender = context.source.sender
        MessageType.INFO.sendMessage(sender, "正在重载配置文件...")
        plugin.reload()
        MessageType.INFO.sendMessage(sender, "重载完成")
        return Command.SINGLE_SUCCESS
    }

    private fun world(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.sender.player
        val world = WorldArgument.getWorld(context, "world_name")
        if (player.bukkitPlayer.world == world) {
            MessageType.FAILED.sendMessage(player, "你已经位于世界 ${world.name}")
            return Command.SINGLE_SUCCESS
        }
        player.bukkitPlayer.teleport(world.spawnLocation)
        return Command.SINGLE_SUCCESS
    }

    fun tpa(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.sender.player
        val target = PlayerInfoArgument.getPlayerInfo(context, "player_name")
        player.teleport.teleport(target.name, TeleportType.TPA)
        return Command.SINGLE_SUCCESS
    }

    fun tpahere(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.sender.player
        val target = PlayerInfoArgument.getPlayerInfo(context, "player_name")
        player.teleport.teleport(target.name, TeleportType.TPAHERE)
        return Command.SINGLE_SUCCESS
    }
}



