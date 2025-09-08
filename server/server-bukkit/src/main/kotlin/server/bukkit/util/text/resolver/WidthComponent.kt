package server.bukkit.util.text.resolver

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextDecoration


class WidthComponent(val symbol: Component, val width: Int) : ComponentLike {
    companion object {
        val EMPTY = WidthComponent(Component.empty(), 0)
    }


    init {
        require(isEmpty() || isBoldSet()) { "invalid bold state" }
    }

    fun isEmpty(): Boolean {
        return symbol == Component.empty()
    }

    fun isBoldSet(): Boolean {
        return symbol.style().decoration(TextDecoration.BOLD) != TextDecoration.State.NOT_SET
    }

    override fun asComponent(): Component {
        return symbol
    }
}
