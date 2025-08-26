package server.bukkit.util.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class BitmapResolver(private val bitmaps: Map<String, Bitmap>) {
    companion object {
        private const val BITMAP = "bitmap"
    }

    fun resolver(): TagResolver {
        return TagResolver.resolver(BITMAP) { arguments, _ ->
            val args = mutableListOf<String>()
            while (arguments.hasNext()) {
                args.add(arguments.pop().value())
            }
            Tag.selfClosingInserting(resolve(args))
        }
    }

    fun resolve(args: String): LengthyComponent {
        return resolve(args.split(":"))
    }

    fun resolve(args: List<String>): LengthyComponent {
        if (args.isEmpty()) throw NullPointerException("bitmap name")
        val bitmap = bitmaps[args[0]]!!
        val sb = StringBuilder()
        if (args.size == 3) {
            val row = parseInt(args[1])
            val nums = splitToInt(args[2])
            if (nums.size >= 2) {
                sb.append(bitmap.getRange(row, nums[0], nums[1]))
            } else {
                sb.append(bitmap.getChar(row, nums[0]))
            }
        } else if (args.size == 2) {
            val nums = splitToInt(args[1])
            if (nums.size >= 2) {
                sb.append(bitmap.getRange(nums[0], nums[1]))
            } else {
                sb.append(bitmap.getChar(nums[0]))
            }
        } else {
            sb.append(bitmap.getChar())
        }
        // 强制防止变粗体 bitmap 不应该有粗体
        val style =
            Style.style().color(NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false).font(bitmap.font.key())
                .build()
        return LengthyComponent(Component.text(sb.toString(), style), bitmap.width)
    }

    private fun parseInt(s: String): Int {
        if (s.isEmpty()) return 0
        return s.toInt()
    }

    private fun splitToInt(arg: String): IntArray {
        val split = arg.split("-")
        val nums = IntArray(split.size)
        for (i in split.indices) {
            nums[i] = parseInt(split[i])
        }
        return nums
    }
}
