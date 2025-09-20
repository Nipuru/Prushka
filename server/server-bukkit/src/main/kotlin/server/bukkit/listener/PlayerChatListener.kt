package server.bukkit.listener

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import server.bukkit.BukkitPlugin
import server.bukkit.MessageType
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.gameplay.player.gamePlayer
import server.bukkit.util.schedule
import server.common.logger.Logger


class PlayerChatListener : Listener {
    @EventHandler
    fun onEvent(event: AsyncChatEvent) {
        event.isCancelled = true
        try {
            val player = event.player.gamePlayer
            val message: String = LegacyComponentSerializer.legacySection().serialize(event.message())
            BukkitPlugin.schedule {
                handleChat(player, message)
            }
        } catch (e: Exception) {
            Logger.error(e.message, e)
        }
    }

    private fun handleChat(player: GamePlayer, message: String) {
        player.core.afk = false
        if (player.chat.isMuted) {
            MessageType.WARNING.sendMessage(player, "你已被系统禁言.")
            return
        }
        if (player.chat.rateLimit()) {
            MessageType.WARNING.sendMessage(player, "你的聊天频率过快, 请稍后再试.")
            return
        }
        if (player.chat.msgTarget != "") {
            player.chat.sendPrivateChat(player.chat.msgTarget, message).thenApply { success ->
                if (!success) {
                    MessageType.FAILED.sendMessage(player, "消息发送失败, 私聊目标无法收到消息, 已退出私聊模式")
                    player.chat.msgTarget = ""
                }
            }

        } else {
            player.chat.sendPublicChat(message).thenApply { success ->
                if (!success) {
                    MessageType.FAILED.sendMessage(player.bukkitPlayer, "消息发送失败。")
                }
            }
        }
    }
}
