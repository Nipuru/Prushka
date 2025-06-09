package server.database.processor

import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.SyncUserProcessor
import server.database.service.OfflineDataService

class PlayerOfflineDataDBProcessor : SyncUserProcessor<server.common.message.PlayerOfflineDataMessage>() {

    override fun handleRequest(bizContext: BizContext, request: server.common.message.PlayerOfflineDataMessage): Any {
        OfflineDataService.insert(request)
        return true
    }

    override fun interest(): String {
        return server.common.message.PlayerOfflineDataMessage::class.java.name
    }
}
