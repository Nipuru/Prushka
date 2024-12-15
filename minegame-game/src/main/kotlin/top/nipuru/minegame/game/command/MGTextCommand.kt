package top.nipuru.minegame.game.command

import top.nipuru.minegame.game.util.component
import org.bukkit.command.CommandSender


/**
 * 向自己打印一串 minimessage 消息
 * Cmd: /mg text <args>...
 *
 * @author Nipuru
 * @since 2024/11/19 15:27
 */
internal class MGTextCommand : AbstractCommand("text", "t") {
    override fun onCommand(sender: CommandSender, args: Array<String>) {
        val message = args.joinToString(" ").component()
        sender.sendMessage(message)
    }
}
