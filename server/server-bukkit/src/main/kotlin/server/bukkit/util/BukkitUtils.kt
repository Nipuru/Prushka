package server.bukkit.util

import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask


/**
 * 注册监听器
 */
fun Listener.register(plugin: Plugin) {
    Bukkit.getPluginManager().registerEvents(this, plugin)
}

/**
 * 获取物品栏空格子数
 */
fun Inventory.getEmptySlot(): Int {
    return storageContents.count { itemStack ->
        itemStack == null || itemStack.isEmpty
    }
}

/**
 * Bukkit 任务调度
 */
fun Plugin.schedule(
    async: Boolean = false,
    delay: Long = 0,
    period: Long = -1,
    block: (BukkitTask) -> Unit
): BukkitTask {
    lateinit var task: BukkitTask

    { block(task) }.let {
        if (async) Bukkit.getScheduler().runTaskTimerAsynchronously(this, it, delay, period)
        else Bukkit.getScheduler().runTaskTimer(this, it, delay, period)
    }.also { task = it }
    return task
}