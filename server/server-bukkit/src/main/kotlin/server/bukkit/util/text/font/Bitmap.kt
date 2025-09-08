package server.bukkit.util.text.font

import net.kyori.adventure.key.Keyed
import kotlin.math.min

class Bitmap(val name: String, val font: Keyed, val width: Int, vararg val chars: String) {

    val string: String

    val size: Int get() = string.length

    val rows: Int get() = chars.size

    init {
        require(chars.isNotEmpty()) { "No characters provided" }
        val sb = StringBuilder()
        for (s in chars) { sb.append(s) }
        string = sb.toString()
    }

    fun getRowSize(row: Int): Int {
        return if (rows > row) chars[row].length else 0
    }

    fun getChar(index: Int = 0): Char {
        return string[index % size]
    }

    fun getChar(row: Int = 0, col: Int = 0): Char {
        val str = chars[row % rows]
        return str[col % str.length]
    }

    fun getRange(startInclude: Int, endExclude: Int): String {
        return string.substring(startInclude % size, min(endExclude, size))
    }

    fun getRange(row: Int, startInclude: Int, endExclude: Int): String {
        val str = chars[row % rows]
        return str.substring(startInclude % str.length, min(endExclude, str.length))
    }
}
