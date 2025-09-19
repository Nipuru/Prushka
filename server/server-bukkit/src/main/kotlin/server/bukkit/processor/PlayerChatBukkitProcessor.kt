package server.bukkit.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import com.alipay.remoting.rpc.protocol.SyncUserProcessor
import org.bukkit.Bukkit
import server.bukkit.gameplay.player.GamePlayerManager
import server.bukkit.gameplay.player.gamePlayer
import server.common.message.PlayerChatMessage
import server.common.message.PlayerPrivateChatMessage

class PlayerChatServerProcessor : AsyncUserProcessor<PlayerChatMessage>() {

    override fun handleRequest(bizContext: BizContext, asyncContext: AsyncContext, request: PlayerChatMessage) {
        for (player in GamePlayerManager.getPlayers()) {
            val manager = player.chat
            manager.receivePublicChat(request.sender, request.fragments)
        }
    }

    override fun interest(): String {
        return PlayerChatMessage::class.java.name
    }
}

class PlayerPrivateChatServerProcessor : SyncUserProcessor<PlayerPrivateChatMessage>() {
    override fun handleRequest(bizContext: BizContext, request: PlayerPrivateChatMessage): Any {
        val sender = GamePlayerManager.getPlayerOrNull(request.sender.uniqueId)
        sender?.chat?.receivePrivateChat(request.sender, request.receiver, request.fragments)
        try {
            val receiver = Bukkit.getPlayerExact(request.receiver)!!.gamePlayer
            receiver.chat.receivePrivateChat(request.sender, request.receiver, request.fragments)
        } catch (ignored: Exception) {
            return false
        } // 有概率玩家在跨服或者离线
        return true
    }


    override fun interest(): String {
        return PlayerPrivateChatMessage::class.java.name
    }
}

