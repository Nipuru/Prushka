package top.nipuru.minegame.game.util

import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin


/**
 * @author Nipuru
 * @since 2024/11/12 18:11
 */
fun Listener.register(plugin: Plugin) {
    Bukkit.getPluginManager().registerEvents(this, plugin)
}

fun Inventory.getEmptySlot(): Int {
    var slot = 0
    for (itemStack in storageContents) {
        if (itemStack == null) {
            ++slot
        } else if (itemStack.type.isAir) {
            ++slot
        } else if (itemStack.amount == 0) {
            ++slot
        }
    }
    return slot
}
