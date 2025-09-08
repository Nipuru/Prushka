package server.bukkit.util.text.resolver

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class SplitResolver(initials: List<WidthComponent>) {
    companion object {
        private const val SPLIT = "split"
    }

    private val splits = arrayOfNulls<WidthComponent>(513)

    init {
        val sorted = initials.sortedBy { it.width }
        for (width in -256..256) {
            push(answer(sorted, width))
        }
    }

    fun resolver(): TagResolver {
        return TagResolver.resolver(SPLIT) { arguments, _ ->
            val width = arguments.popOr("Expected to find a split width").value().toInt()
            Tag.selfClosingInserting(resolve(width))
        }
    }

    fun resolve(width: Int): Component {
        if (width < -256 || width > 256) {
            val builder = Component.text()
            var left = width
            while (left != 0) {
                if (left > 256) {
                    builder.append(resolve(256))
                    left -= 256
                } else if (left < -256) {
                    builder.append(resolve(-256))
                    left += 256
                } else {
                    builder.append(resolve(left))
                    left = 0
                }
            }
            return builder.build()
        } else {
            return splits[index(width)]!!.symbol
        }
    }

    private fun index(width: Int): Int {
        return width + 256
    }

    private fun answer(sorted: List<WidthComponent>, split: Int): WidthComponent {
        if (split == 0) {
            return WidthComponent.EMPTY
        }
        val reverse = split > 0
        var i = if (reverse) sorted.size - 1 else 0
        var left = split
        val builder = Component.text()
        // 强制防止变粗体 split 不应该有粗体
        builder.style(Style.style().decoration(TextDecoration.BOLD, false).build())
        while (left != 0) {
            val sp = sorted[i]
            while (reverse && sp.width <= left || !reverse && sp.width >= left) {
                left -= sp.width
                builder.append(sp.symbol)
            }
            i += (if (reverse) -1 else 1)
        }

        return WidthComponent(builder.build(), split)
    }

    private fun push(component: WidthComponent) {
        if (component.width <= 255 && component.width >= -258) {
            splits[index(component.width)] = component
        }
    }
}
