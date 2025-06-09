package server.bukkit.gameplay.chat.formatter

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import server.bukkit.gameplay.chat.Fragment
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.nms.deserializeItemStacks
import server.bukkit.nms.serialize
import server.common.message.shared.PlayerInfoMessage
import java.util.regex.Pattern

class ShowItemFormatter : MessagePattern(Pattern.compile("#展示([0-9]*)#")) {
    override fun parse(player: GamePlayer, vararg args: String?): Fragment {
        val itemStacks = arrayOfNulls<ItemStack>(1)
        itemStacks[0] = getItemStack(player, args[0]!!)
        val data = itemStacks.serialize()
        return Fragment(data)
    }

    override fun format(sender: PlayerInfoMessage, receiver: GamePlayer, fragment: Fragment): Component {
        val data = fragment.getArg<ByteArray>(0)
        val itemStack: ItemStack = data.deserializeItemStacks()[0]!!
        val builder = Component.text()
        builder.append(Component.text("["))
            .append(itemStack.displayName())
        if (itemStack.amount > 1) {
            builder.append(Component.text(" x" + itemStack.amount))
        }
        builder.append(Component.text("]"))
        builder.color(NamedTextColor.WHITE)
        return builder.build()
    }

    private fun getItemStack(player: GamePlayer, slotString: String): ItemStack {
        try {
            val slot = slotString.toInt()
            val itemStack = player.bukkitPlayer.inventory.getItem(slot)
            if (itemStack != null) return itemStack
        } catch (ignored: Exception) {
        }
        return ItemStack(Material.AIR)
    }
}
