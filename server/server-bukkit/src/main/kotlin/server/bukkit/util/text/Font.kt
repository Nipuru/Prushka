package server.bukkit.util.text

import net.kyori.adventure.key.Keyed
import org.bukkit.NamespacedKey

class Font private constructor(val font: Keyed, val character: Char, val width: Float, val boldWidth: Float) {

    companion object {

        val DEFAULT: Keyed = NamespacedKey.minecraft("default")

        fun steve(font: Keyed = DEFAULT, character: Char, width: Int): Font {
            return Font(font, character, width.toFloat(), (width + 1).toFloat())
        }

        fun steves(font: Keyed = DEFAULT, string: String, width: Int): List<Font> {
            val fonts = ArrayList<Font>(string.length)
            for (element in string) { fonts += steve(font, element, width) }
            return fonts
        }

        fun steves(bitmap: Bitmap, width: Int): List<Font> {
            return steves(bitmap.font, bitmap.string, width)
        }

        fun slim(font: Keyed = DEFAULT, character: Char, width: Float): Font {
            return Font(font, character, width, width + 0.5f)
        }

        fun slims(font: Keyed = DEFAULT, string: String, width: Float): List<Font> {
            val fonts = ArrayList<Font>(string.length)
            for (element in string) { fonts += slim(font, element, width) }
            return fonts
        }

        fun slims(bitmap: Bitmap, width: Float): List<Font> {
            return slims(bitmap.font, bitmap.string, width)
        }

        fun font(font: Keyed = DEFAULT, character: Char, width: Float, boldWidth: Float): Font {
            return Font(font, character, width, boldWidth)
        }

        fun fonts(font: Keyed = DEFAULT, string: String, width: Float, boldWidth: Float): List<Font> {
            val fonts = ArrayList<Font>(string.length)
            for (element in string) { fonts += font(font, element, width, boldWidth) }
            return fonts
        }

        fun fonts(bitmap: Bitmap, width: Float, boldWidth: Float): List<Font> {
            return fonts(bitmap.font, bitmap.string, width, boldWidth)
        }
    }
}
