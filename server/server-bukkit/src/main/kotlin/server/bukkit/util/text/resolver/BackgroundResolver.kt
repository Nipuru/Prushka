package server.bukkit.util.text.resolver

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.tag.Modifying
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tree.Node
import server.bukkit.util.text.font.Font
import server.bukkit.util.text.font.FontRepository
import kotlin.math.ceil

class BackgroundResolver(
    private val tag: String,
    private val splitResolver: SplitResolver,
    private val fontRepository: FontRepository,
    private val prefix: WidthComponent,
    private val suffix: WidthComponent,
    middle: List<WidthComponent>,
    private val centre: Boolean,
    private val covered: Boolean,
    private val style: Style
) {

    companion object {
        private val OFFSET = TextColor.fromHexString("#fffefd")!!
    }

    private val middle: List<WidthComponent> = middle.sortedBy { it.width }

    fun resolver(): TagResolver {
        return TagResolver.resolver(tag, ExtensionBGTag())
    }

    private inner class ExtensionBGTag : Modifying {
        private val rootNode = 0
        private var font = Font.DEFAULT
        private var bold = false
        private var italic = false


        override fun visit(current: Node, depth: Int) {
            if (depth != rootNode) return
            // 找到 样式节点 获取根节点样式类型
            font = current.getFont(font)
            bold = current.isBold(bold)
            italic = current.isItalic(italic)
        }

        override fun apply(current: Component, depth: Int): Component {
            if (depth != rootNode) return Component.empty()
            val head = Component.text()
            val bg = Component.text()
            bg.style(style)

            val sw = suffix.width.toFloat()
            val width = fontRepository.getTotalWidth(current, bold, italic, font)

            val split_0 = splitResolver.resolve(-1)

            var fw = 0f
            var mid: Component = Component.empty()
            for (text in middle) {
                val lw = width - fw
                val c = (lw / text.width).toInt()
                if (c == 0) continue
                fw += (c * text.width).toFloat()
                for (i in 0 until c) {
                    mid = mid.append(text.symbol)
                        .append(split_0)
                }
            }
            if (fw < width && !covered) {
                val text = middle[middle.size - 1]
                fw += text.width.toFloat()
                mid = mid.append(text.symbol)
                    .append(split_0)
            }
            var back = sw + fw
            if (centre) {
                back += (fw - width) / 2
            } else if (fw + sw < width) {
                back += width - fw - sw
            }
            if (style.color() != null && style.color()!!.value() == OFFSET.value()) back -= 1f

            val splitText = splitResolver.resolve(-back.toInt() - 1)
            val splitEnd = splitResolver.resolve(ceil((back - width - 1).toDouble()).toInt())


            bg.append(prefix.symbol)
                .append(split_0)
                .append(mid)
                .append(suffix.symbol)

            head.append(bg)
                .append(splitText)
                .append(current)
                .append(splitEnd)
            return head.build()
        }
    }


}
