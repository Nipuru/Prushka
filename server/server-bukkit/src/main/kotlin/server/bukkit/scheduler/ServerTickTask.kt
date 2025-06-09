package server.bukkit.scheduler

import org.bukkit.Bukkit
import server.bukkit.gameplay.player.GamePlayers
import server.bukkit.plugin

/**
 * 主线程调度 每 1-tick 执行一次
 */

class ServerTickTask : Runnable {
    fun schedule() {
        Bukkit.getScheduler().runTaskTimer(plugin, this, 1L, 1L)
    }
    override fun run() {
        GamePlayers.tick()
    }
}
