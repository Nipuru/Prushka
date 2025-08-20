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
import server.bukkit.BukkitPlugin
import server.bukkit.MessageType
import server.bukkit.command.argument.GamePlayerArgument
import server.bukkit.command.argument.PlayerInfoArgument
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.gameplay.misc.ResourcePack
import server.bukkit.gameplay.misc.setResourcePack
import server.bukkit.gameplay.skin.PlayerSkin
import server.bukkit.util.*
import server.common.logger.Logger
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
            .then(literal("resourcepack")
                .executes(::resourcepack))
            .then(literal("skin")
                .then(argument("player_name", GamePlayerArgument)
                    .then(argument("skin", StringArgumentType.string())
                        .executes(::skin))))
            .build())
    }

    /**
     * 发送文本消息
     * /prushka text <文本>
     */
    private fun text(context: CommandContext<CommandSourceStack>): Int {
        val sender = context.source.sender
        val message = StringArgumentType.getString(context, "args").component()
        sender.sendMessage(message)
        return Command.SINGLE_SUCCESS
    }

    /**
     * 重载配置文件
     * /prushka reload
     */
    private fun reload(context: CommandContext<CommandSourceStack>): Int {
        val sender = context.source.sender
        MessageType.INFO.sendMessage(sender, "正在重载配置文件...")
        BukkitPlugin.reload()
        MessageType.INFO.sendMessage(sender, "重载完成")
        return Command.SINGLE_SUCCESS
    }

    /**
     * 世界传送
     * /prushka world <世界名>
     */
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

    /**
     * 玩家tpa
     * /prushka tpa <玩家名>
     */
    fun tpa(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        val target = context.getArgument<PlayerInfoMessage>("player_name")
        player.teleport.teleport(target.name, TeleportType.TPA)
        return Command.SINGLE_SUCCESS
    }

    /**
     * 玩家tphere
     * /prushka tpahere <玩家名>
     */
    fun tpahere(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        val target = context.getArgument<PlayerInfoMessage>("player_name")
        player.teleport.teleport(target.name, TeleportType.TPAHERE)
        return Command.SINGLE_SUCCESS
    }

    /**
     * 下载资源包
     * /prushka resourcepack
     */
    fun resourcepack(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        ResourcePack.getServerPack().whenComplete { pack, throwable ->
            if (throwable != null) {
                Logger.error(throwable.message, throwable)
            }
            if (pack == null) {
                MessageType.FAILED.sendMessage(context.source.sender, "资源包不存在或获取资源包信息时发生错误")
                return@whenComplete
            }
            player.setResourcePack(pack)
            MessageType.INFO.sendMessage(context.source.sender, "资源包已发送")
        }
        MessageType.INFO.sendMessage(context.source.sender, "正在获取资源包信息...")
        return Command.SINGLE_SUCCESS
    }

    fun skin(context: CommandContext<CommandSourceStack>): Int {
        val player = context.getArgument<GamePlayer>("player_name")
        ArgumentTypes.playerProfiles()
        val skinName = context.getArgument<String>("skin")
        PlayerSkin.create(skinName).whenComplete { skin, throwable ->
            if (throwable != null) {
                Logger.error(throwable.message, throwable)
            }
            if (skin == null) {
                MessageType.FAILED.sendMessage(context.source.sender, "皮肤信息不存在或获取皮肤信息时发生错误")
                return@whenComplete
            }
            player.skin.setSkin(skin)
            MessageType.INFO.sendMessage(context.source.sender, "玩家 ${player.name} 的皮肤已更新")
        }
        MessageType.INFO.sendMessage(context.source.sender, "正在获取皮肤信息...")
        return Command.SINGLE_SUCCESS
    }
}



