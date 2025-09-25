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
            player.chat.receiveChat(request.sender, request.fragments)
        }
    }

    override fun interest(): String {
        return PlayerChatMessage::class.java.name
    }
}

class PlayerPrivateChatServerProcessor : SyncUserProcessor<PlayerPrivateChatMessage>() {
    override fun handleRequest(bizContext: BizContext, request: PlayerPrivateChatMessage): Boolean {
        val sender = GamePlayerManager.getPlayerOrNull(request.sender.uniqueId)
        sender?.chat?.receivePrivateChat(request.sender, request.receiver, request.fragments, true)
        Bukkit.getPlayerExact(request.receiver)?.gamePlayer?.apply {
            chat.receivePrivateChat(request.sender, request.receiver, request.fragments, false)
            return true
        }
        return false
    }


    override fun interest(): String {
        return PlayerPrivateChatMessage::class.java.name
    }
}

