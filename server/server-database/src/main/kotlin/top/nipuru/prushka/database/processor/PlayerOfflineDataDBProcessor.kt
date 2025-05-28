package top.nipuru.prushka.database.processor

import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.SyncUserProcessor
import top.nipuru.prushka.common.message.PlayerOfflineDataMessage
import top.nipuru.prushka.database.service.OfflineDataService

class PlayerOfflineDataDBProcessor : SyncUserProcessor<PlayerOfflineDataMessage>() {

    override fun handleRequest(bizContext: BizContext, request: PlayerOfflineDataMessage): Any {
        OfflineDataService.insert(request)
        return true
    }

    override fun interest(): String {
        return PlayerOfflineDataMessage::class.java.name
    }
}
