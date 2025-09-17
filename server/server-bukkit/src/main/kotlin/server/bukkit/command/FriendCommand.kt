package server.bukkit.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands.argument
import io.papermc.paper.command.brigadier.Commands.literal
import server.bukkit.command.argument.PlayerInfoArgument
import server.bukkit.util.CommandTree
import server.bukkit.MessageType
import server.common.message.PlayerInfoMessage
import java.util.concurrent.CompletableFuture


/**
 * @author Nipuru
 * @since 2024/11/20 09:52
 */
@Suppress("UnstableApiUsage")
class FriendCommand : CommandTree {

    override val root: LiteralCommandNode<CommandSourceStack> = literal("friend")
        .then(literal("add")
            .then(argument("player_name", PlayerInfoArgument)
                .executes(::add)))
        .then(literal("remove")
            .then(argument("player_name", PlayerInfoArgument)
                .executes(::remove)))
        .then(literal("accept")
            .then(argument("player_name", PlayerInfoArgument)
                .executes(::accept)))
        .then(literal("reject")
            .then(argument("player_name", PlayerInfoArgument)
                .executes(::reject)))
        .build()

    private fun add(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        context.getArgument<CompletableFuture<PlayerInfoMessage?>>("player_name").thenAccept { target ->
            if (target == null) {
                MessageType.FAILED.sendMessage(player, "玩家不存在")
                return@thenAccept
            }
            val targetship = player.friend.getFriend(target.playerId)
            if (targetship != null) {
                MessageType.FAILED.sendMessage(player, "玩家 ${target.name} 已经是你的好友了")
                return@thenAccept
            }
            player.friend.requestFriend(target.name, target.playerId, target.dbId)
            MessageType.ALLOW.sendMessage(player, "已向玩家 ${target.name} 发送好友请求")
        }

        return Command.SINGLE_SUCCESS
    }
    
    private fun remove(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        context.getArgument<CompletableFuture<PlayerInfoMessage?>>("player_name").thenAccept { target ->
            if (target == null) {
                MessageType.FAILED.sendMessage(player, "玩家不存在")
                return@thenAccept
            }
            val targetship = player.friend.getFriend(target.playerId)
            if (targetship == null) {
                MessageType.FAILED.sendMessage(player, "玩家 ${target.name} 不在你的好友列表")
                return@thenAccept
            }
            player.friend.deleteFriend(target.name, target.playerId, target.dbId)
            MessageType.ALLOW.sendMessage(player, "你删除了好友 ${target.name}")
        }

        return Command.SINGLE_SUCCESS
    }
    
    private fun accept(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        context.getArgument<CompletableFuture<PlayerInfoMessage?>>("player_name").thenAccept { target ->
            if (target == null) {
                MessageType.FAILED.sendMessage(player, "玩家不存在")
                return@thenAccept
            }
            val targetRequest = player.friend.getReceivedFriendRequest(target.playerId)
            if (targetRequest == null) {
                MessageType.FAILED.sendMessage(player, "没有来自玩家 ${target.name} 的好友请求")
                return@thenAccept
            }
            player.friend.acceptFriend(target.name, target.playerId, target.dbId)
            MessageType.ALLOW.sendMessage(player, "你接受了来自玩家 ${target.name} 的好友请求")
        }
        return Command.SINGLE_SUCCESS
    }
    
    private fun reject(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        context.getArgument<CompletableFuture<PlayerInfoMessage?>>("player_name").thenAccept { target ->
            if (target == null) {
                MessageType.FAILED.sendMessage(player, "玩家不存在")
                return@thenAccept
            }
            val targetRequest = player.friend.getReceivedFriendRequest(target.playerId)
            if (targetRequest == null) {
                MessageType.FAILED.sendMessage(player, "没有来自玩家 ${target.name} 的好友请求")
                return@thenAccept
            }
            player.friend.rejectFriend(target.name, target.playerId, target.dbId)
            MessageType.ALLOW.sendMessage(player, "你拒绝了来自玩家 ${target.name} 的好友请求")
        }
        return Command.SINGLE_SUCCESS
    }

}


