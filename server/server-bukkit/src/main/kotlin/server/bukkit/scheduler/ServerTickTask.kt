package server.bukkit.scheduler

import org.bukkit.scheduler.BukkitTask
import server.bukkit.gameplay.player.GamePlayerManager
import server.bukkit.util.ScheduleTask

/**
 * 主线程调度 每 1-tick 执行一次
 */
class ServerTickTask : ScheduleTask(delay = 1L, period = 1L) {
    override fun run(task: BukkitTask) = GamePlayerManager.tick()
}
