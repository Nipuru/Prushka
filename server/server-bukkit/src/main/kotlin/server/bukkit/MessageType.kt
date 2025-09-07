package server.bukkit

import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.*
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.util.text.FixedWidthResolver.Position
import server.bukkit.util.text.Font
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

    fun sendMessage(sender: GamePlayer, vararg args: Any?) {
        val hexString = TextColor.color(color.rgb).asHexString()
        val message = "<$hexString>" + args.joinToString()
        sender.bukkitPlayer.sendMessage(message.component())
    }
}

fun String.component() : TextComponent {
    return Component.text().let {
        it.append(BukkitPlugin.textFactory.miniMessage.deserialize(this))
        it.style(Style.style()
            .decoration(TextDecoration.ITALIC, false)
            .build())
        it.build()
    }
}

/**
 * 追加字符串
 */
fun Component.append(str: String): Component {
    return this.append(str.component())
}

/**
 * 追加 HoverEvent
 */
fun Component.hoverEvent(str: String): Component {
    return this.hoverEvent(str.component())
}

/**
 * 文本位移
 * @param pixel 像素
 */
fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.split(pixel: Int) : ComponentBuilder<C, B> {
    return this.append("<split:$pixel>".component())
}

/**
 * 添加图片
 * @param name 图片名称 st_bitmap config_id
 * @param row 行
 * @param column 列
 */
fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.bitmap(name: String, row: Int = 0, column: Int = 0): ComponentBuilder<C, B> {
    return this.append("<bitmap:$name:$row:$column>".component())
}

/**
 * 固定宽度
 * @param text 文本
 * @param width 宽度
 * @param position 对其方式
 */
fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.fixedWidth(text: String, width: Int, position: Position = Position.LEFT): ComponentBuilder<C, B> {
    return this.append("<fixed_width:$position:$width>${text}</fixed_width>".component())
}

/**
 * 获取组件宽度
 * @param parentBold 父组件是否粗体
 * @param parentItalic 父组件是否斜体
 * @param parentFont 父组件字体
 */
fun ComponentLike.getWidth(parentBold: Boolean = false, parentItalic: Boolean = false, parentFont: Keyed = Font.DEFAULT): Int {
    return BukkitPlugin.textFactory.font.getTotalWidth(asComponent(), parentBold, parentItalic, parentFont)
}
