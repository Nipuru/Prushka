package server.bukkit.util.text

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextDecoration
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.InputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO
import kotlin.math.roundToInt


class FontRepository(private val resourceResolver: (fileName: String) -> InputStream?) {
    private val default = Int2ObjectOpenHashMap<Font>()
    private val uniform = Int2ObjectOpenHashMap<Font>()
    private val fonts = mutableMapOf<Keyed, Int2ObjectOpenHashMap<Font>>()

    init {
        registerDefault()
        registerUniform()
    }

    fun register(font: Font) {
        if (font.font == Font.DEFAULT) {
            this.default[font.codePoint] = font
        } else if (font.font == Font.UNIFORM) {
            this.uniform[font.codePoint] = font
        } else {
            this.fonts.computeIfAbsent(font.font) { Int2ObjectOpenHashMap() }[font.codePoint] = font
        }
    }

    fun getFont(font: Keyed, ch: Int): Font? {
        return if (font == Font.DEFAULT) {
            this.default[ch] ?: this.uniform[ch]
        } else if (font == Font.UNIFORM) {
            this.uniform[ch]
        } else {
            this.fonts[font]?.get(ch)
        }
    }

    fun getTotalWidth(component: Component, parentBold: Boolean, parentItalic: Boolean, parentFont: Keyed): Int {
        val text = if (component is TextComponent) component.content() else ""
        val font = component.font() ?: parentFont
        val style = component.style()
        var bold = style.parseDecoration(TextDecoration.BOLD, parentBold)
        val italic = style.parseDecoration(TextDecoration.ITALIC, parentItalic)
        var width = 0
        for (c in text.toCharArray()) {
            val f = getFont(font, c.code) ?: continue
            if (bold) width += 1
            if (italic) width += 1
            width += f.width
        }
        if (component.children().isNotEmpty()) {
            for (c in component.children()) {
                width += getTotalWidth(c, bold, italic, font)
            }
        }
        return width
    }

    private fun registerDefault() {
        // 注册 default 字体 来源 minecraft_default.json
        val inputStream = resourceResolver("minecraft_default.json") ?: error("Unable to find file minecraft_default.json")
        inputStream.bufferedReader().use { reader ->
            JsonParser.parseReader(reader).asJsonObject.getAsJsonArray("providers")
        }.forEach { provider ->
            provider as JsonObject
            val imageName = provider.get("file").asString.run {
                if (contains('/')) substringAfterLast('/') else this
            }
            val imageStream = resourceResolver(imageName) ?: error("Unable to find file $imageName")
            val image = imageStream.buffered().use { inputStream ->
                ImageIO.read(inputStream)
            }
            val chars = provider.getAsJsonArray("chars")
            val height = image.height / chars.size()
            chars.forEachIndexed { row, charElement ->
                val str = charElement.asString
                val width = image.width / str.length
                str.forEachIndexed { column, char ->
                    image.getSubimage(width * column, height * row, width, height).removeEmptyWidth()?.let { i ->
                        register(Font.font(Font.DEFAULT, char.code, i.width + 1))
                    }
                }
            }
        }
    }

    private fun registerUniform() {
        // 注册 uniform 字体 来源 unifont.hex
        val inputStream = resourceResolver("unifont.hex") ?: error("Unable to find file uniform.hex")
        inputStream.bufferedReader().use { reader ->
            reader.readLines()
        }.forEach { line ->
            val (unicode, hex) = line.split(":")
            val code = unicode.toInt(16)
            val size = hex.length * 4
            val buf = ByteBuffer.allocate(size)
            hex.forEachIndexed { index, c ->
                val char = c.digitToInt(16)
                for (i in (0..<4).reversed()) {
                    buf.put(((char shr i) and 1).toByte())
                }
            }
            val width = size / 16
            var image = BufferedImage(width, 16, BufferedImage.TYPE_INT_ARGB)
            for (row in 0..<16) {
                for (column in 0..<width) {
                    if (buf[column + row * width] == 1.toByte()) image.setRGB(column, row, Color.WHITE.rgb)
                }
            }
            image.removeEmptyWidth()?.also {
                register(Font.font(Font.UNIFORM, code, (it.width / 2.0).roundToInt() + 1))
            }
        }
    }
}
