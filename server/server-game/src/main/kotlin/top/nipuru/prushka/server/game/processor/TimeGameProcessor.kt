package top.nipuru.prushka.server.game.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import top.nipuru.prushka.server.common.message.DebugTimeNotify
import top.nipuru.prushka.server.game.time.TimeManager


/**
 * @author Nipuru
 * @since 2024/11/28 10:29
 */
class DebugTimeGameProcessor : AsyncUserProcessor<DebugTimeNotify>() {
    override fun handleRequest(context: BizContext, asyncContext: AsyncContext, message: DebugTimeNotify) {
        TimeManager.debugTime = message.time
    }

    override fun interest(): String {
        return DebugTimeNotify::class.java.name
    }
}