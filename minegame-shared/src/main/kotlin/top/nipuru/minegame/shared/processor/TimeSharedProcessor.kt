package top.nipuru.minegame.shared.processor

import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.SyncUserProcessor
import top.nipuru.minegame.common.message.GetTimeRequest
import top.nipuru.minegame.shared.time.TimeManager


/**
 * @author Nipuru
 * @since 2024/11/28 10:24
 */
class GetTimeSharedProcessor : SyncUserProcessor<GetTimeRequest>() {
    override fun handleRequest(context: BizContext, message: GetTimeRequest): Long {
        return TimeManager.debugTime()
    }

    override fun interest(): String {
        return GetTimeRequest::class.java.name
    }
}
