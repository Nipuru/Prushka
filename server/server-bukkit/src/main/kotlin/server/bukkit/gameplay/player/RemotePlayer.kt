package server.bukkit.gameplay.player

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import server.bukkit.scheduler.AudienceMessenger.send
import server.bukkit.util.text.TextFactory
import server.common.message.AudienceMessage.Message.SystemChat
import server.common.message.PlayerInfoMessage


/**
 * 代表集群上的一个玩家 可能在线也可能不在线。
 * 用于向当前玩家发送消息。
 *
 * @param playerInfo 玩家信息。
 */
val PlayerInfoMessage.remotePlayer: RemotePlayer get() = RemotePlayer(this)

class RemotePlayer(val playerInfo: PlayerInfoMessage) : Audience {
    override fun sendMessage(message: Component) {
        val serialized = TextFactory.instance.miniMessage.serialize(message)
        val request = SystemChat(receiver = playerInfo.uniqueId, message = serialized)
        send(request)
    }

    override fun clearResourcePacks() {
        throw UnsupportedOperationException("")
    }
}