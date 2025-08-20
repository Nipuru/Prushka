package server.bukkit.scheduler

import org.bukkit.Bukkit
import server.bukkit.BukkitPlugin
import server.bukkit.gameplay.player.GamePlayerManager

/**
 * 主线程调度 每 1-tick 执行一次
 */

class ServerTickTask : Runnable {
    fun schedule() {
        Bukkit.getScheduler().runTaskTimer(BukkitPlugin, this, 1L, 1L)
    }
    override fun run() {
        GamePlayerManager.tick()
    }
}
