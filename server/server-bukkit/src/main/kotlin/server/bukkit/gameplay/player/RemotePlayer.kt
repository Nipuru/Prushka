package server.bukkit.gameplay.player

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import server.bukkit.scheduler.SystemChatSender
import server.bukkit.util.text.TextFactory
import server.common.message.PlayerInfoMessage
import server.common.message.SystemChatMessage
import java.util.*


/**
 *
 * 扩展函数，用于从 PlayerInfoMessage 对象中获取 RemotePlayer 实例。
 *
 * @author Nipuru
 * @since 2025/09/14 17:13
 */
val PlayerInfoMessage.remotePlayer: RemotePlayer get() = RemotePlayer(uniqueId)
val Iterable<PlayerInfoMessage>.remotePlayers: List<RemotePlayer> get() = map { RemotePlayer(it.uniqueId) }
val Array<PlayerInfoMessage>.remotePlayers: List<RemotePlayer> get() = map { RemotePlayer(it.uniqueId) }

/**
 * 代表集群上的一个玩家。
 * 用于向当前玩家发送消息。
 *
 * @param uniqueId 玩家的唯一标识符。
 */
class RemotePlayer(val uniqueId: UUID) : Audience {

    override fun sendMessage(message: Component) {
        val serialized = TextFactory.instance.miniMessage.serialize(message)
        val request = SystemChatMessage.Message(receiver = uniqueId, message = serialized)
        SystemChatSender.send(request)
    }
}