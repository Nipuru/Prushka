package server.bukkit

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.gameplay.player.gamePlayer
import server.bukkit.gameplay.player.remotePlayer
import server.bukkit.util.text.component
import server.common.message.PlayerInfoMessage
import java.awt.Color

/**
 * 前缀状态
 * ALLOW正常 — 某事件成功执行，或权限允许，正向的提示
 * FAILED失败 - 某事件存在一些条件无法满足,导致不能进行
 * INFO提示 - 对于某些信息的中性提醒,或某事件的中性提示
 * WARNING警告 - 某事件没有权限,某事件被绝对禁止. 强烈的语气！
 */
enum class MessageType(val prefix: String, val color: Color) {
    ALLOW("<bitmap:system_prefix:0>", Color(185, 236, 90)),
    FAILED("<bitmap:system_prefix:3>",Color(242, 223, 84)),
    INFO("<bitmap:system_prefix:2>",Color(98, 205, 228)),
    WARNING("<bitmap:system_prefix:1>",Color(236, 90, 93)),
    DEBUG("<bitmap:system_prefix:4>",Color(128, 128, 128));

    /** 向远程玩家发送消息 */
    fun sendMessage(receiver: PlayerInfoMessage, message: String) {
        val player = receiver.gamePlayer?.bukkitPlayer
        if (player != null) sendMessage(player, message)
        else sendMessage(receiver.remotePlayer, message)
    }

    /** 向 [GamePlayer] 发送消息 */
    fun sendMessage(receiver: GamePlayer, message: String) {
        sendMessage(receiver.bukkitPlayer, message)
    }

    /** 发送消息 */
    fun sendMessage(receiver: Audience, message: String) {
        receiver.sendMessage(createComponent(message))
    }

    /** 创建消息组件 */
    fun createComponent(message: String): Component {
        val hexString = TextColor.color(color.rgb).asHexString()
        return "$prefix<$hexString>$message".component()
    }
}

