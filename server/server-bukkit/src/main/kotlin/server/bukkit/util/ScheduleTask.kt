package server.bukkit.util

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin


/**
 * 定时任务基类
 * @author Nipuru
 * @since 2025/09/25 10:52
 */
abstract class ScheduleTask(val async: Boolean = false, val delay: Long = 0, val period: Long = -1): Runnable {
    fun schedule(plugin: Plugin) {
        if (async) Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, delay, period)
        else Bukkit.getScheduler().runTaskTimer(plugin, this, delay, period)
    }
}
