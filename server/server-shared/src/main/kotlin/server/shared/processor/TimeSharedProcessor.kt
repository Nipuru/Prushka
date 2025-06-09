package server.shared.processor

import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.SyncUserProcessor
import server.common.message.GetTimeRequest


/**
 * @author Nipuru
 * @since 2024/11/28 10:24
 */
class GetTimeSharedProcessor : SyncUserProcessor<GetTimeRequest>() {
    override fun handleRequest(context: BizContext, message: GetTimeRequest): Long {
        return server.shared.time.TimeManager.debugTime()
    }

    override fun interest(): String {
        return GetTimeRequest::class.java.name
    }
}
