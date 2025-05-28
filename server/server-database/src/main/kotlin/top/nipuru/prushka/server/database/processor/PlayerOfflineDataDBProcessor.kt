package top.nipuru.prushka.server.database.processor

import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.SyncUserProcessor
import top.nipuru.prushka.server.common.message.PlayerOfflineDataMessage
import top.nipuru.prushka.server.database.service.OfflineDataService

class PlayerOfflineDataDBProcessor : SyncUserProcessor<top.nipuru.prushka.server.common.message.PlayerOfflineDataMessage>() {

    override fun handleRequest(bizContext: BizContext, request: top.nipuru.prushka.server.common.message.PlayerOfflineDataMessage): Any {
        OfflineDataService.insert(request)
        return true
    }

    override fun interest(): String {
        return top.nipuru.prushka.server.common.message.PlayerOfflineDataMessage::class.java.name
    }
}
