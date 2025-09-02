package server.bukkit.util

import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin


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
