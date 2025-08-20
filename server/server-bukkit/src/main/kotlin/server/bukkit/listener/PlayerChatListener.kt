package server.bukkit.listener

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import server.bukkit.BukkitPlugin
import server.bukkit.gameplay.player.gamePlayer
import server.common.logger.Logger


class PlayerChatListener : Listener {
    @EventHandler
    fun onEvent(event: AsyncChatEvent) {
        event.isCancelled = true
        val message: String = LegacyComponentSerializer.legacySection().serialize(event.message())
        try {
            handleChat(event.player, message)
        } catch (e: Exception) {
            Logger.error(e.message, e)
        }
    }

    private fun handleChat(bukkitPlayer: Player, message: String) {
        val player = bukkitPlayer.gamePlayer
        Bukkit.getScheduler().runTask(BukkitPlugin, Runnable {
            player.core.afk = false
            if (player.chat.isMuted) {
                return@Runnable
            }
            if (player.chat.hasMsgTarget()) {
                player.chat.sendPrivate(player.chat.msgTarget, message)
            } else {
                player.chat.sendPublic(message)
            }
        })
    }
}
