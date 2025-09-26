package server.bukkit.util

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.concurrent.Executor


/**
* @author Nipuru
* @since 2025/09/26 18:04
*/
class ServerThreadExecutor(private val plugin: Plugin) : Executor {
    override fun execute(command: Runnable) {
        if (Bukkit.isPrimaryThread()) command.run()
        else Bukkit.getScheduler().runTask(plugin, command)
    }
}