package server.bukkit.listener

import com.destroystokyo.paper.event.server.ServerExceptionEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import server.bukkit.logger.LogServer
import server.bukkit.logger.logger


/**
 * @author Nipuru
 * @since 2025/06/30 19:45
 */
class ServerExceptionListener : Listener {

    @EventHandler
    fun onEvent(event: ServerExceptionEvent) {
        val error = event.exception.cause?:return

        LogServer.reportError(error)
    }
}