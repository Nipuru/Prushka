package server.bukkit.util.text

import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextDecoration

class FontRepository {
    private val fonts = mutableMapOf<Keyed, MutableMap<Char, Font>>()

    fun register(font: Font) {
        this.fonts.computeIfAbsent(font.font) { HashMap() }[font.character] = font
    }

    fun getFont(font: Keyed, ch: Char): Font? {
        val map = fonts[font] ?: return null
        return map[ch]
    }

    fun getWidth(font: Keyed, text: String, bold: Boolean): Float {
        var width = 0f
        for (c in text.toCharArray()) {
            val f = getFont(font, c) ?: continue
            width += if (bold) f.boldWidth else f.width
        }
        return width
    }

    fun getTotalWidth(component: Component, parentBold: Boolean, parentFont: Keyed): Float {
        val text = if (component is TextComponent) component.content() else ""
        val font = component.font() ?: parentFont
        val state = component.decorations()[TextDecoration.BOLD]
        var bold = false
        if (state == TextDecoration.State.TRUE) {
            bold = true
        } else if (state == TextDecoration.State.NOT_SET) {
            bold = parentBold
        }
        var width = getWidth(font, text, bold)
        if (component.children().isNotEmpty()) {
            for (c in component.children()) {
                width += getTotalWidth(c, bold, font)
            }
        }
        return width
    }
}
