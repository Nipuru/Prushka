package top.nipuru.prushka.game.command

import top.nipuru.prushka.game.util.component
import org.bukkit.command.CommandSender


/**
 * 向自己打印一串 minimessage 消息
 * Cmd: /prushka text <args>...
 *
 * @author Nipuru
 * @since 2024/11/19 15:27
 */
internal class PrushkaTextCommand : AbstractCommand("text", "t") {
    override fun onCommand(sender: CommandSender, args: Array<String>) {
        val message = args.joinToString(" ").component()
        sender.sendMessage(message)
    }
}
