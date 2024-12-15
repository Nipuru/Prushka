package top.nipuru.minegame.database.processor

import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.SyncUserProcessor
import top.nipuru.minegame.common.message.PlayerOfflineDataMessage
import top.nipuru.minegame.database.offline.OfflineDataManager

class PlayerOfflineDataDBProcessor : SyncUserProcessor<PlayerOfflineDataMessage>() {

    override fun handleRequest(bizContext: BizContext, request: PlayerOfflineDataMessage): Any {
        OfflineDataManager.insert(request)
        return true
    }

    override fun interest(): String {
        return PlayerOfflineDataMessage::class.java.name
    }
}
