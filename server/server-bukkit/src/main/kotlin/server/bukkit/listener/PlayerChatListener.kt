package server.bukkit.listener

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import server.bukkit.gameplay.player.GamePlayers
import server.bukkit.logger.logger
import server.bukkit.plugin


class PlayerChatListener : Listener {
    @EventHandler
    fun onEvent(event: AsyncChatEvent) {
        event.isCancelled = true
        val message: String = LegacyComponentSerializer.legacySection().serialize(event.message())
        try {
            handleChat(event.player, message)
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
    }

    private fun handleChat(bukkitPlayer: org.bukkit.entity.Player, message: String) {
        val player = GamePlayers.getPlayer(bukkitPlayer.uniqueId)
        Bukkit.getScheduler().runTask(plugin, Runnable {
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
