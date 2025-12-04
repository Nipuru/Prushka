package server.bukkit.nms.menu

import net.minecraft.world.Container
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ClickType


/**
 * @author Nipuru
 * @since 2025/11/07 17:37
 */
class CustomChestMenu(
    containerId: Int,
    inventory: Inventory,
    private val customChestContainer: CustomChestContainer
) : ChestMenu(customChestContainer.menuType, containerId, inventory, customChestContainer, customChestContainer.containerRows) {

    override fun clicked(slotId: Int, button: Int, clickType: ClickType, player: Player) {
        if (customChestContainer.clicked(slotId, button, clickType, player)) return
        super.clicked(slotId, button, clickType, player)
    }

    override fun addStandardInventorySlots(container: Container, x: Int, y: Int) {
        if ((this.customChestContainer).hideInventory) return
        super.addStandardInventorySlots(container, x, y)
    }
}