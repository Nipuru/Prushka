package server.bukkit.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.command.UnknownCommandEvent
import server.bukkit.MessageType
import server.bukkit.util.text.TextFactory


/**
 * @author Nipuru
 * @since 2025/09/20 16:59
 */
class UnknownCommandListener : Listener {

    @EventHandler
    fun onEvent(event: UnknownCommandEvent) {
        // 使用服务器警告消息展示指令错误
        val message = event.message() ?: return
        var string = TextFactory.instance.miniMessage.serialize(message)
        string = string.replaceFirst("<red>", "") // 移除红色前缀
        event.message(MessageType.WARNING.createComponent(string))
    }
}