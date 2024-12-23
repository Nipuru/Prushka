package top.nipuru.prushka.game

import top.nipuru.prushka.game.util.component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.CommandSender
import java.awt.Color

/**
 * 前缀状态
 * ALLOW正常 — 某事件成功执行，或权限允许，正向的提示
 * FAILED失败 - 某事件存在一些条件无法满足,导致不能进行
 * INFO提示 - 对于某些信息的中性提醒,或某事件的中性提示
 * WARNING警告 - 某事件没有权限,某事件被绝对禁止. 强烈的语气！
 */
enum class MessageType(val color: Color) {
    ALLOW(Color(185, 236, 90)),
    FAILED(Color(242, 223, 84)),
    INFO(Color(98, 205, 228)),
    WARNING(Color(236, 90, 93));

    fun sendMessage(sender: CommandSender?, vararg args: Any?) {
        val hexString = TextColor.color(color.rgb).asHexString()
        val message = "<$hexString>" + args.joinToString()
        sender?.sendMessage(message.component())
    }
}
