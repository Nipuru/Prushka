package server.bukkit.util.text

import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.internal.parser.node.TagNode
import net.kyori.adventure.text.minimessage.tag.Inserting
import net.kyori.adventure.text.minimessage.tree.Node


/**
 * @author Nipuru
 * @since 2025/08/26 14:20
 */

fun Node?.isBold(): Boolean {
    if (this == null) return false
    val style = this.getStyle()
    if (style != null) {
        if (style.decorations()[TextDecoration.BOLD] == TextDecoration.State.TRUE) {
            return true
        }
    }
    return this.parent().isBold()
}

fun Node?.getFont(): Keyed {
    if (this == null) {
        return Font.DEFAULT
    }
    val style = this.getStyle()
    if (style?.font() != null) {
        return style.font()!!
    }

    return this.parent().getFont()
}

fun Node?.getStyle(): Style? {
    if (this is TagNode) {
        val tag = this.tag()
        if (tag is Inserting) {
            val component: Component = tag.value()
            return component.style()
        }
    }
    return null
}