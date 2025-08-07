package server.bukkit.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import server.bukkit.MessageType
import server.bukkit.command.argument.PlayerInfoArgument
import server.common.message.PlayerInfoMessage


/**
 * @author Nipuru
 * @since 2024/11/20 09:52
 */
@Suppress("UnstableApiUsage")
object FriendCommand {

    fun register(registrar: Commands) {
        registrar.register(Commands.literal("target")
            .then(Commands.literal("add")
                .then(Commands.argument("player_name", PlayerInfoArgument)
                .executes(::add)))
            .then(Commands.literal("remove")
                .then(Commands.argument("player_name", PlayerInfoArgument)
                    .executes(::remove)))
            .then(Commands.literal("accept")
                .then(Commands.argument("player_name", PlayerInfoArgument)
                    .executes(::accept)))
            .then(Commands.literal("reject")
                .then(Commands.argument("player_name", PlayerInfoArgument)
                    .executes(::reject)))
            .build())
    }

    private fun add(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        val target = context.getArgument<PlayerInfoMessage>("player_name")
        val targetship = player.friend.getFriend(target.playerId)
        if (targetship != null) {
            MessageType.FAILED.sendMessage(player, "玩家 ${target.name} 已经是你的好友了")
            return Command.SINGLE_SUCCESS
        }
        player.friend.requestFriend(target.name, target.playerId, target.dbId)
        MessageType.ALLOW.sendMessage(player, "已向玩家 ${target.name} 发送好友请求")
        return Command.SINGLE_SUCCESS
    }
    
    private fun remove(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        val target = context.getArgument<PlayerInfoMessage>("player_name")
        val targetship = player.friend.getFriend(target.playerId)
        if (targetship == null) {
            MessageType.FAILED.sendMessage(player, "玩家 ${target.name} 不在你的好友列表")
            return Command.SINGLE_SUCCESS
        }
        player.friend.deleteFriend(target.name, target.playerId, target.dbId)
        MessageType.ALLOW.sendMessage(player, "你删除了好友 ${target.name}")
        return Command.SINGLE_SUCCESS
    }
    
    private fun accept(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        val target = context.getArgument<PlayerInfoMessage>("player_name")
        val targetRequest = player.friend.getReceivedFriendRequest(target.playerId)
        if (targetRequest == null) {
            MessageType.FAILED.sendMessage(player, "没有来自玩家 ${target.name} 的好友请求")
            return Command.SINGLE_SUCCESS
        }
        player.friend.acceptFriend(target.name, target.playerId, target.dbId)
        MessageType.ALLOW.sendMessage(player, "你接受了来自玩家 ${target.name} 的好友请求")
        return Command.SINGLE_SUCCESS
    }
    
    private fun reject(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        val target = context.getArgument<PlayerInfoMessage>("player_name")
        val targetRequest = player.friend.getReceivedFriendRequest(target.playerId)
        if (targetRequest == null) {
            MessageType.FAILED.sendMessage(player, "没有来自玩家 ${target.name} 的好友请求")
            return Command.SINGLE_SUCCESS
        }
        player.friend.rejectFriend(target.name, target.playerId, target.dbId)
        MessageType.ALLOW.sendMessage(player, "你拒绝了来自玩家 ${target.name} 的好友请求")
        return Command.SINGLE_SUCCESS
    }
    
}


