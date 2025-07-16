package server.database.processor

import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.SyncUserProcessor
import server.common.message.PlayerOfflineDataMessage
import server.database.service.OfflineDataServiceImpl

class PlayerOfflineDataDBProcessor : SyncUserProcessor<PlayerOfflineDataMessage>() {

    override fun handleRequest(bizContext: BizContext, request: PlayerOfflineDataMessage): Any {
        OfflineDataServiceImpl.insert(request.playerId, request.module, request.data)
        return true
    }

    override fun interest(): String {
        return PlayerOfflineDataMessage::class.java.name
    }
}
