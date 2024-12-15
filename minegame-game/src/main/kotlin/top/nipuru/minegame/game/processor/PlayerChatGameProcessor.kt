package top.nipuru.minegame.game.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import com.alipay.remoting.rpc.protocol.SyncUserProcessor
import top.nipuru.minegame.common.message.PlayerChatMessage
import top.nipuru.minegame.common.message.PlayerPrivateChatMessage
import top.nipuru.minegame.game.gameplay.player.GamePlayer
import top.nipuru.minegame.game.gameplay.player.GamePlayers
import org.bukkit.Bukkit

class PlayerChatServerProcessor : AsyncUserProcessor<PlayerChatMessage>() {

    override fun handleRequest(bizContext: BizContext, asyncContext: AsyncContext, request: PlayerChatMessage) {
        for (player in GamePlayers.players) {
            val manager = player.chat
            manager.receivePublic(request.sender, request.fragments)
        }
    }

    override fun interest(): String {
        return PlayerChatMessage::class.java.name
    }
}

class PlayerPrivateChatServerProcessor : SyncUserProcessor<PlayerPrivateChatMessage>() {
    override fun handleRequest(bizContext: BizContext, request: PlayerPrivateChatMessage): Any {
        try {
            val bukkitPlayer = Bukkit.getPlayerExact(request.receiver)
            val player: GamePlayer = GamePlayers.getPlayer(bukkitPlayer!!.uniqueId)
            val manager = player.chat
            if (!manager.couldReceivePrivate(request.sender)) {
                return PlayerPrivateChatMessage.DENY
            }
            manager.receivePrivate(request.sender, request.receiver, request.fragments)
        } catch (ignored: Exception) {
        } // 有概率玩家在跨服或者离线

        return PlayerPrivateChatMessage.SUCCESS
    }


    override fun interest(): String {
        return PlayerPrivateChatMessage::class.java.name
    }
}

