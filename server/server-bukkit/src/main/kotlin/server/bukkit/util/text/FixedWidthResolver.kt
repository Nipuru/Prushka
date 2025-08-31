package server.bukkit.util.text

import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Modifying
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tree.Node
import java.util.*

class FixedWidthResolver(private val splitResolver: SplitResolver, private val fontRepository: FontRepository) {
    companion object {
        private const val FIXED_WIDTH = "fixed_width"
    }

    fun resolver(): TagResolver {
        return TagResolver.resolver(FIXED_WIDTH) { arguments, _ ->
            val position = Position.valueOf(
                arguments.popOr("Expected to find a fixed position").value().uppercase(Locale.getDefault())
            )
            val fixedWidth = arguments.popOr("Expected to find a fixed width").value().toFloat()
            FixedWidthTag(position, fixedWidth)
        }
    }

    enum class Position {
        LEFT, CENTER, RIGHT
    }

    private inner class FixedWidthTag(private val position: Position, private val fixedWidth: Float) : Modifying {
        val rootNode: Int = 0
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
            // 运用宽度并返回组件
            val textWidth = fontRepository.getTotalWidth(current, bold, italic, font)
            var left: Component = Component.empty()
            var right: Component = Component.empty()
            if (position == Position.LEFT) {
                right = splitResolver.resolve((fixedWidth - textWidth).toInt())
            } else if (position == Position.RIGHT) {
                left = splitResolver.resolve((fixedWidth - textWidth).toInt())
            } else {
                val rightWidth = ((fixedWidth - textWidth) / 2).toInt()
                val leftWidth = (fixedWidth - textWidth - rightWidth).toInt()
                left = splitResolver.resolve(leftWidth)
                right = splitResolver.resolve(rightWidth)
            }
            return Component.text().append(left).append(current).append(right).build()
        }
    }
}
