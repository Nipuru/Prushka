package server.bukkit.util.text

import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.internal.parser.node.TagNode
import net.kyori.adventure.text.minimessage.tag.Inserting
import net.kyori.adventure.text.minimessage.tree.Node
import java.awt.image.BufferedImage


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
    if (this is TagNode) {
        val tag = this.tag()
        if (tag is Inserting) {
            val component: Component = tag.value()
            return component.style()
        }
    }
    return null
}

fun Style.parseDecoration(decoration: TextDecoration, default: Boolean): Boolean = when (decoration(decoration)) {
    TextDecoration.State.NOT_SET -> default
    TextDecoration.State.FALSE -> false
    TextDecoration.State.TRUE -> true
}

fun BufferedImage.removeEmptyWidth(): BufferedImage? {
    var widthA = 0
    var widthB = width

    for (i1 in 0..<width) {
        for (i2 in 0..<height) {
            if ((getRGB(i1, i2) and -0x1000000) ushr 24 > 0) {
                if (widthA < i1) widthA = i1
                if (widthB > i1) widthB = i1
            }
        }
    }
    val finalWidth = widthA - widthB + 1

    if (finalWidth <= 0) return null

    return getSubimage(widthB, 0, finalWidth, height)
}