package server.bukkit.util.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextDecoration


class LengthyComponent(val symbol: Component, val length: Int) : ComponentLike {
    companion object {
        val EMPTY = LengthyComponent(Component.empty(), 0)
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
