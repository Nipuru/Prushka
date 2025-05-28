package top.nipuru.prushka.server.shared.processor

import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.SyncUserProcessor
import top.nipuru.prushka.server.common.message.GetTimeRequest
import top.nipuru.prushka.server.shared.time.TimeManager


/**
 * @author Nipuru
 * @since 2024/11/28 10:24
 */
class GetTimeSharedProcessor : SyncUserProcessor<GetTimeRequest>() {
    override fun handleRequest(context: BizContext, message: GetTimeRequest): Long {
        return top.nipuru.prushka.server.shared.time.TimeManager.debugTime()
    }

    override fun interest(): String {
        return GetTimeRequest::class.java.name
    }
}
