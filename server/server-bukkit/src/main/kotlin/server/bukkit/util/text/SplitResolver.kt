package server.bukkit.util.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class SplitResolver(initials: List<LengthyComponent>) {
    companion object {
        private const val SPLIT = "split"
    }

    private val splits = arrayOfNulls<LengthyComponent>(513)

    init {
        val sorted = initials.sortedBy { it.length }
        for (i in -256..256) {
            push(answer(sorted, i))
        }
    }

    fun resolver(): TagResolver {
        return TagResolver.resolver(SPLIT) { arguments, _ ->
            val length = arguments.popOr("Expected to find a split length").value().toInt()
            Tag.selfClosingInserting(resolve(length))
        }
    }

    fun resolve(length: Int): Component {
        if (length < -256 || length > 256) {
            val builder = Component.text()
            var left = length
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
            return splits[index(length)]!!.symbol
        }
    }

    private fun index(length: Int): Int {
        return length + 256
    }

    private fun answer(sorted: List<LengthyComponent>, length: Int): LengthyComponent {
        if (length == 0) {
            return LengthyComponent.EMPTY
        }
        val reverse = length > 0
        var i = if (reverse) sorted.size - 1 else 0
        var left = length
        val builder = Component.text()
        // 强制防止变粗体 split 不应该有粗体
        builder.style(Style.style().decoration(TextDecoration.BOLD, false).build())
        while (left != 0) {
            val sp = sorted[i]
            while (reverse && sp.length <= left || !reverse && sp.length >= left) {
                left -= sp.length
                builder.append(sp.symbol)
            }
            i += (if (reverse) -1 else 1)
        }

        return LengthyComponent(builder.build(), length)
    }

    private fun push(split: LengthyComponent) {
        if (split.length <= 256 && split.length >= -256) {
            splits[index(split.length)] = split
        }
    }
}
