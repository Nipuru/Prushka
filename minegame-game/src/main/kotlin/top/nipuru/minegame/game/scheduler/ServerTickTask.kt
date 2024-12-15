package top.nipuru.minegame.game.scheduler

import top.nipuru.minegame.game.gameplay.player.GamePlayers
import top.nipuru.minegame.game.plugin
import org.bukkit.Bukkit

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
