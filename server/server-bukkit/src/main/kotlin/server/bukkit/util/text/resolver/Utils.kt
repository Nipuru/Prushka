package server.bukkit.util.text.resolver

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

fun Node?.isBold(default: Boolean): Boolean {
    if (this == null) return default
    val style = this.getStyle()
    if (style != null) {
        if (style.decorations()[TextDecoration.BOLD] == TextDecoration.State.TRUE) {
            return true
        } else if (style.decoration(TextDecoration.ITALIC) == TextDecoration.State.FALSE) {
            return false
        }
    }
    return this.parent().isBold(default)
}

fun Node?.isItalic(default: Boolean): Boolean {
    if (this == null) return default
    val style = this.getStyle()
    if (style != null) {
        if (style.decorations()[TextDecoration.ITALIC] == TextDecoration.State.TRUE) {
            return true
        } else if (style.decoration(TextDecoration.ITALIC) == TextDecoration.State.FALSE) {
            return false
        }
    }
    return this.parent().isItalic(default)
}

fun Node?.getFont(default: Keyed): Keyed {
    if (this == null) {
        return default
    }
    val style = this.getStyle()
    if (style?.font() != null) {
        return style.font()!!
    }

    return this.parent().getFont(default)
}

fun Node?.getStyle(): Style? {
    @Suppress("UnstableApiUsage")
    if (this is TagNode) {
        val tag = this.tag()
        if (tag is Inserting) {
            val component: Component = tag.value()
            return component.style()
        }
    }
    return null
}



