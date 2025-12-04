package server.bukkit.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands.argument
import io.papermc.paper.command.brigadier.Commands.literal
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import kotlinx.coroutines.future.await
import org.bukkit.World
import server.bukkit.BukkitPlugin
import server.bukkit.MessageType
import server.bukkit.command.argument.GamePlayerArgument
import server.bukkit.command.argument.PlayerInfoArgument
import server.bukkit.command.argument.RankArgument
import server.bukkit.gameplay.misc.ResourcePack
import server.bukkit.gameplay.misc.setResourcePack
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.gameplay.skin.PlayerSkin
import server.bukkit.util.CommandTree
import server.bukkit.util.text.component
import server.bukkit.util.text.getWidth
import server.common.logger.Logger
import server.common.message.PlayerInfoMessage
import server.common.message.TeleportType
import server.common.sheet.StRank


/**
 * 管理员命令根节点
 * Cmd: /prushka
 *
 * @author Nipuru
 * @since 2024/11/19 15:11
 */
class OperatorCommand : CommandTree {

    override val root: LiteralCommandNode<CommandSourceStack> = literal("prushka")
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
        .then(literal("rank")
            .then(argument("player_name", GamePlayerArgument)
                .then(argument("rank", RankArgument)
                    .executes(::rank))))
        .build()

    /**
     * 发送文本消息
     * /prushka text <文本>
     */
    private fun text(context: CommandContext<CommandSourceStack>) {
        val sender = context.source.sender
        val message = StringArgumentType.getString(context, "args").component()
        MessageType.INFO.sendMessage(sender, "文本宽度: ${message.getWidth()}")
        sender.sendMessage(message)
    }

    /**
     * 重载配置文件
     * /prushka reload
     */
    private fun reload(context: CommandContext<CommandSourceStack>) {
        val sender = context.source.sender
        MessageType.INFO.sendMessage(sender, "正在重载配置文件...")
        BukkitPlugin.reload()
        MessageType.ALLOW.sendMessage(sender, "重载完成")
    }

    /**
     * 世界传送
     * /prushka world <世界名>
     */
    private fun world(context: CommandContext<CommandSourceStack>) {
        val player = context.source.gamePlayer
        val world = context.getArgument<World>("world_name")
        if (player.bukkitPlayer.world == world) {
            MessageType.FAILED.sendMessage(player, "你已经位于世界 ${world.name}")
            return
        }
        player.bukkitPlayer.teleport(world.spawnLocation)
    }

    /**
     * 玩家tpa
     * /prushka tpa <玩家名>
     */
    private suspend fun tpa(context: CommandContext<CommandSourceStack>) {
        val player = context.source.gamePlayer
        val target = context.getFutureArgument<PlayerInfoMessage?>("player_name")
        if (target == null) {
            MessageType.FAILED.sendMessage(player, "玩家不存在")
            return
        }
        player.teleport.teleport(target.name, TeleportType.TPA)
        MessageType.INFO.sendMessage(player, "正在将你传送至玩家 ${target.name}")
    }

    /**
     * 玩家tphere
     * /prushka tpahere <玩家名>
     */
    private suspend fun tpahere(context: CommandContext<CommandSourceStack>) {
        val player = context.source.gamePlayer
        val target = context.getFutureArgument<PlayerInfoMessage?>("player_name")
        if (target == null) {
            MessageType.FAILED.sendMessage(player, "玩家不存在")
            return
        }
        player.teleport.teleport(target.name, TeleportType.TPAHERE)
        MessageType.INFO.sendMessage(player, "正在将玩家 ${target.name} 传送至你")
    }

    /**
     * 下载资源包
     * /prushka resourcepack
     */
    private suspend fun resourcepack(context: CommandContext<CommandSourceStack>) {
        val player = context.source.gamePlayer
        try {
            MessageType.INFO.sendMessage(context.source.sender, "正在获取资源包信息...")
            val pack = ResourcePack.getServerPack().await()
            if (pack == null) {
                MessageType.FAILED.sendMessage(context.source.sender, "资源包不存在或获取资源包信息时发生错误")
                return
            }
            player.setResourcePack(pack)
            MessageType.ALLOW.sendMessage(context.source.sender, "资源包已发送")
        } catch (e: Exception) {
            Logger.error(e.message, e)
        }
    }

    /**
     * 设置玩家皮肤
     * /prushka skin <player_name> <skin_name>
     */
    private suspend fun skin(context: CommandContext<CommandSourceStack>) {
        val player = context.getArgument<GamePlayer>("player_name")
        val skinName = context.getArgument<String>("skin")
        try {
            MessageType.INFO.sendMessage(context.source.sender, "正在获取皮肤信息...")
            val skin = PlayerSkin.create(skinName).await()
            if (skin == null) {
                MessageType.FAILED.sendMessage(context.source.sender, "皮肤信息不存在或获取皮肤信息时发生错误")
                return
            }
            player.skin.setSkin(skin)
            MessageType.ALLOW.sendMessage(context.source.sender, "玩家 ${player.name} 的皮肤已更新")
        } catch (e: Exception) {
            Logger.error(e.message, e)
        }
    }

    /**
     * 设置玩家称号
     * /prushka rank <player_name> <rank>
     */
    private fun rank(context: CommandContext<CommandSourceStack>) {
        val player = context.getArgument<GamePlayer>("player_name")
        val rank = context.getArgument<StRank>("rank")
        player.core.rankId = rank.configId
        MessageType.ALLOW.sendMessage(context.source.sender, "成功将玩家 ${player.name} 的称号设置为 ${rank.name}")
    }
}



