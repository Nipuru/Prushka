package server.bukkit.gameplay.chat

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.kyori.adventure.text.Component
import server.bukkit.gameplay.chat.formatter.MessageFormatter
import server.bukkit.gameplay.chat.formatter.MessagePattern
import server.bukkit.gameplay.chat.formatter.PlainFormatter
import server.bukkit.gameplay.chat.formatter.ShowItemFormatter
import server.bukkit.gameplay.player.GamePlayer
import server.common.message.FragmentMessage
import server.common.message.PlayerInfoMessage
import java.util.*
import java.util.regex.Matcher

object MessageFormat {
    private val formatters = mutableListOf<MessageFormatter>()
    private val plainFormatter = PlainFormatter()
    private val plainFormatterIdx = 0

    init {
        formatters.add(plainFormatterIdx, plainFormatter)
        formatters.add(ShowItemFormatter())
    }

    fun parse(sender: GamePlayer, message: String): Array<FragmentMessage> {
        val index2Fragment = Int2ObjectOpenHashMap<Pair<Int, FragmentMessage>>()
        for (i in formatters.indices) {
            val formatter = formatters[i]
            if (formatter !is MessagePattern) continue
            val matcher: Matcher = formatter.pattern.matcher(message)
            while (matcher.find()) {
                val args = arrayOfNulls<String>(matcher.groupCount())
                for (j in args.indices) {
                    args[j] = matcher.group(j + 1)
                }
                val fragment = formatter.parse(sender, *args)
                if (fragment == null) {
                    matcher.region(matcher.start() + 1, matcher.regionEnd())
                    continue
                }
                val fragmentMessage = FragmentMessage(i, fragment.args)
                index2Fragment.putIfAbsent(matcher.start(), matcher.end() to fragmentMessage)
            }
        }
        val fragments: MutableList<FragmentMessage> = LinkedList()
        var start = 0
        var i = 0
        while (i < message.length) {
            val entry = index2Fragment[i]
            if (entry == null) {
                i++
                continue
            }
            val plainFragment = plainFormatter.parse(sender, message.substring(start, i))
            if (plainFragment != null) fragments.add(FragmentMessage(plainFormatterIdx, plainFragment.args))
            fragments.add(entry.second)
            start = entry.first
            i = start - 1
            i++
        }
        if (start < message.length) {
            val plainFragment = plainFormatter.parse(sender, message.substring(start))
            if (plainFragment != null) {
                fragments.add(FragmentMessage(plainFormatterIdx, plainFragment.args))
            }
        }

        return fragments.toTypedArray<FragmentMessage>()
    }

    fun format(sender: PlayerInfoMessage, receiver: GamePlayer, fragments: Array<FragmentMessage>): Component {
        val builder = Component.text()
        for (fragment in fragments) {
            val formatter = formatters[fragment.formatterIdx]
            val component = formatter.format(sender, receiver, Fragment(*fragment.args))
            builder.append(component)
        }
        return builder.build()
    }
}
