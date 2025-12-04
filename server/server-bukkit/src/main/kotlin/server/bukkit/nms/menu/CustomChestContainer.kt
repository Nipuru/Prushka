package server.bukkit.nms.menu

import net.minecraft.core.NonNullList
import net.minecraft.network.chat.Component
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import org.bukkit.Location
import org.bukkit.craftbukkit.entity.CraftHumanEntity
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.InventoryHolder


/**
 * @author Nipuru
 * @since 2025/11/10 11:36
 */
class CustomChestContainer : Container, MenuProvider, InventoryHolder {

    val containerRows: Int = TODO()
    val menuType: MenuType<*> = TODO()
    val hideInventory: Boolean = TODO()
    private val transaction = mutableListOf<HumanEntity>()
    private var maxStack = Container.MAX_STACK
    private val size = containerRows * 9
    private val items = NonNullList.withSize(size, ItemStack.EMPTY)
    private var displayName: Component = Component.empty()

    fun clicked(
        slotId: Int,
        button: Int,
        clickType: ClickType,
        player: Player
    ): Boolean {
        TODO("Not yet implemented")
    }

    fun setDisplayName(displayName: Component) {
        this.displayName = displayName
    }

    override fun setChanged() {

    }

    override fun stillValid(p0: Player): Boolean {
        return true
    }

    override fun clearContent() {
        items.clear()
        this.setChanged()
    }

    override fun getContainerSize(): Int {
        return size
    }

    override fun isEmpty(): Boolean {
        return items.all { it.isEmpty }
    }

    override fun getItem(index: Int): ItemStack {
        return if (index >= 0 && index < items.size) items[index] else ItemStack.EMPTY
    }

    override fun removeItem(index: Int, count: Int): ItemStack {
        val itemStack = ContainerHelper.removeItem(this.items, index, count)
        if (!itemStack.isEmpty) {
            this.setChanged()
        }
        return itemStack
    }

    override fun removeItemNoUpdate(index: Int): ItemStack {
        val itemStack = items[index]
        if (itemStack.isEmpty) {
            return ItemStack.EMPTY
        } else {
            items[index] = ItemStack.EMPTY
            return itemStack
        }
    }

    override fun setItem(index: Int, stack: ItemStack) {
        items[index] = stack
        stack.limitSize(this.getMaxStackSize(stack))
        this.setChanged()
    }

    override fun getContents(): MutableList<ItemStack> {
        return items
    }

    override fun onOpen(player: CraftHumanEntity) {
        transaction += player
    }

    override fun onClose(player: CraftHumanEntity) {
        transaction -= player
    }

    override fun getViewers(): MutableList<HumanEntity> {
        return transaction
    }

    override fun getOwner(): InventoryHolder {
        return this
    }

    override fun setMaxStackSize(size: Int) {
        this.maxStack = size
    }

    override fun getMaxStackSize(): Int {
        return maxStack
    }

    override fun getInventory(): org.bukkit.inventory.Inventory {
        return CraftInventory(this)
    }

    override fun createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
        return CustomChestMenu(containerId, inventory, this)
    }

    override fun getDisplayName(): Component {
        return displayName
    }

    override fun getLocation(): Location? {
        return null
    }



}