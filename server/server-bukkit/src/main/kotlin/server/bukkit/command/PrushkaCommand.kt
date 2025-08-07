package server.bukkit.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.Commands.argument
import io.papermc.paper.command.brigadier.Commands.literal
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.World
import server.bukkit.MessageType
import server.bukkit.command.argument.PlayerInfoArgument
import server.bukkit.plugin
import server.bukkit.util.component
import server.common.message.PlayerInfoMessage
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
        registrar.register(literal("prushka")
            .requireOperator()
            .then(literal("text")
                .then(argument("args", StringArgumentType.greedyString())
                    .executes(::text)))
            .then(literal("reload")
                .executes(::reload))
            .then(literal("world")
                .then(argument("world_name", ArgumentTypes.world())
                    .executes(::world)))
            .then(literal("tpa")
                .then(argument("player_name", PlayerInfoArgument)
                    .executes(::tpa)))
            .then(literal("tpahere")
                .then(argument("player_name", PlayerInfoArgument)
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
        val player = context.source.gamePlayer
        val world = context.getArgument<World>("world_name")
        if (player.bukkitPlayer.world == world) {
            MessageType.FAILED.sendMessage(player, "你已经位于世界 ${world.name}")
            return Command.SINGLE_SUCCESS
        }
        player.bukkitPlayer.teleport(world.spawnLocation)
        return Command.SINGLE_SUCCESS
    }

    fun tpa(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        val target = context.getArgument<PlayerInfoMessage>("player_name")
        player.teleport.teleport(target.name, TeleportType.TPA)
        return Command.SINGLE_SUCCESS
    }

    fun tpahere(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        val target = context.getArgument<PlayerInfoMessage>("player_name")
        player.teleport.teleport(target.name, TeleportType.TPAHERE)
        return Command.SINGLE_SUCCESS
    }
}



