package server.bukkit.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import server.bukkit.MessageType
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.gameplay.player.GamePlayers
import server.common.message.PlayerInfoMessage


/**
 * @author Nipuru
 * @since 2024/11/20 09:52
 */
class FriendCommand : AbstractCommand("friend") {
    override fun canConsoleExecute() = false

    init {
        subCommand(object : Abstract("add") {
            override fun handle(sender: GamePlayer, friend: PlayerInfoMessage) {
                val friendship = sender.friend.getFriend(friend.playerId)
                if (friendship != null) {
                    MessageType.FAILED.sendMessage(sender.bukkitPlayer, "玩家 ${friend.name} 已经是你的好友了")
                    return
                }
                sender.friend.requestFriend(friend.name, friend.playerId, friend.dbId)
                MessageType.ALLOW.sendMessage(sender.bukkitPlayer, "已向玩家 ${friend.name} 发送好友请求")
            }
        })
        subCommand(object : Abstract("remove") {
            override fun handle(sender: GamePlayer, friend: PlayerInfoMessage) {
                val friendship = sender.friend.getFriend(friend.playerId)
                if (friendship == null) {
                    MessageType.FAILED.sendMessage(sender.bukkitPlayer, "玩家 ${friend.name} 不在你的好友列表")
                    return
                }
                sender.friend.deleteFriend(friend.name, friend.playerId, friend.dbId)
                MessageType.ALLOW.sendMessage(sender.bukkitPlayer, "你删除了好友 ${friend.name}")
            }
        })
        subCommand(object : Abstract("accept") {
            override fun handle(sender: GamePlayer, friend: PlayerInfoMessage) {
                val friendRequest = sender.friend.getReceivedFriendRequest(friend.playerId)
                if (friendRequest == null) {
                    MessageType.FAILED.sendMessage(sender.bukkitPlayer, "没有来自玩家 ${friend.name} 的好友请求")
                    return
                }
                sender.friend.acceptFriend(friend.name, friend.playerId, friend.dbId)
                MessageType.ALLOW.sendMessage(sender.bukkitPlayer, "你接受了来自玩家 ${friend.name} 的好友请求")
            }
        })
        subCommand(object : Abstract("reject") {
            override fun handle(sender: GamePlayer, friend: PlayerInfoMessage) {
                val friendRequest = sender.friend.getReceivedFriendRequest(friend.playerId)
                if (friendRequest == null) {
                    MessageType.FAILED.sendMessage(sender.bukkitPlayer, "没有来自玩家 ${friend.name} 的好友请求")
                    return
                }
                sender.friend.rejectFriend(friend.name, friend.playerId, friend.dbId)
                MessageType.ALLOW.sendMessage(sender.bukkitPlayer, "你拒绝了来自玩家 ${friend.name} 的好友请求")
            }
        })
    }

    private abstract class Abstract(name: String) : AbstractCommand(name) {
        override fun onCommand(sender: CommandSender, args: Array<String>) {
            sender as Player
            sendUsageIf(sender, "/friend add <玩家名>") { args.size != 1 }
            async(sender) {
                val friend = getPlayerInfo(sender, args[0])
                sync(sender) {
                    val player = GamePlayers.getPlayer(sender.uniqueId)
                    handle(player, friend)
                }
            }
        }

        override fun onTabComplete(sender: CommandSender, args: Array<String>): List<String> {
            return when (args.size) {
                // TODO 全服玩家名
                1 -> Bukkit.getOnlinePlayers()
                    .map { player -> player.name }
                    .filter { it.contains(args[0], true) }

                else -> emptyList()
            }
        }

        abstract fun handle(sender: GamePlayer, friend: PlayerInfoMessage)
    }
}


