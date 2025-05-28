package top.nipuru.prushka.server.common.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import top.nipuru.prushka.server.common.message.RequestMessage
import top.nipuru.prushka.server.common.message.createResponse
import java.util.concurrent.ConcurrentHashMap

/**
 * RequestMessage 分发处理
 *
 * @author Nipuru
 * @since 2024/10/24 13:08
 */
class RequestDispatcher : AsyncUserProcessor<RequestMessage>() {
    private val handlers: MutableMap<String, Handler<*>> = ConcurrentHashMap()

    fun registerHandler(handler: Handler<*>) {
        require(!handlers.containsKey(handler.interest().name)) { "Handler " + handler.interest() + " is already registered" }
        handlers[handler.interest().name] = handler
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleRequest(bizCtx: BizContext, asyncCtx: AsyncContext, request: RequestMessage) {
        val handler = handlers[request.className]
            ?: throw NullPointerException("Handler " + request.className + " not exist")
        val data = request.getData<Any>()
        handler as Handler<Any>
        handler.handle(ResponseContext(asyncCtx), data)
    }

    override fun interest(): String {
        return RequestMessage::class.java.name
    }

    interface Handler<T> {
        fun handle(asyncCtx: ResponseContext, request: T)
        fun interest(): Class<T>
    }

    class ResponseContext(private val asyncCtx: AsyncContext) {
        fun sendResponse(data: Any?) {
            asyncCtx.sendResponse(createResponse(data))
        }

        fun sendException(ex: Throwable) {
            asyncCtx.sendException(ex)
        }
    }
}
