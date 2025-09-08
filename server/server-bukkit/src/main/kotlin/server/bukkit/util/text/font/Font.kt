package server.bukkit.util.text.font

import net.kyori.adventure.key.Keyed
import org.bukkit.NamespacedKey

class Font private constructor(val font: Keyed, val codePoint: Int, val width: Int) {

    companion object {

        val DEFAULT: Keyed = NamespacedKey.minecraft("default")
        val UNIFORM: Keyed = NamespacedKey.minecraft("uniform")

        fun font(font: Keyed = DEFAULT, codePoint: Int, width: Int): Font {
            return Font(font, codePoint, width)
        }

        fun fonts(font: Keyed = DEFAULT, string: String, width: Int): List<Font> {
            val fonts = ArrayList<Font>(string.length)
            for (element in string) { fonts += font(font, element.code, width) }
            return fonts
        }

        fun fonts(bitmap: Bitmap, width: Int): List<Font> {
            return fonts(bitmap.font, bitmap.string, width)
        }
    }
}
