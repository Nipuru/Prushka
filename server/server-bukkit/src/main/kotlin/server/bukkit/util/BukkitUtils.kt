package server.bukkit.util

import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
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
 * 定时任务基类
 */
abstract class ScheduleTask(val async: Boolean = false, val delay: Long = 0, val period: Long = -1) {
    protected abstract fun run(task: BukkitTask)
    fun schedule(plugin: Plugin): BukkitTask {
        return plugin.schedule(async, delay, period, this::run)
    }
}

/**
 * Bukkit 任务调度
 */
fun Plugin.schedule(async: Boolean = false, delay: Long = 0, period: Long = -1, block: (BukkitTask) -> Unit): BukkitTask {
    lateinit var task: BukkitTask
    { block(task) }.let {
        if (async) Bukkit.getScheduler().runTaskTimerAsynchronously(this, it, delay, period)
        else Bukkit.getScheduler().runTaskTimer(this, it, delay, period)
    }.also { task = it }
    return task
}

/**
 * 命令树
 */
interface CommandTree {
    val root: LiteralCommandNode<CommandSourceStack>
    val description: String? get() = null
    val aliases: Collection<String> get() = emptyList()

    fun register(plugin: Plugin) {
        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            commands.registrar().register(root, description, aliases)
        }
    }
}