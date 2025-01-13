package top.nipuru.prushka.broker.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncMultiInterestUserProcessor
import net.afyer.afybroker.core.util.AbstractInvokeCallback
import net.afyer.afybroker.core.util.BoltUtils
import net.afyer.afybroker.server.Broker
import net.afyer.afybroker.server.proxy.BrokerClientItem
import top.nipuru.prushka.common.ClientType
import top.nipuru.prushka.common.message.*

class RequestMessageRouter : AsyncMultiInterestUserProcessor<RequestMessageContainer>() {

    override fun handleRequest(bizCtx: BizContext, asyncCtx: AsyncContext, request: RequestMessageContainer) {

        val clientProxy = route(request) ?: throw Exception("No server available for request: $request")

        if (BoltUtils.hasResponse(bizCtx)) {
            clientProxy.invokeWithCallback(request.request, object : AbstractInvokeCallback() {
                override fun onResponse(result: Any) {
                    asyncCtx.sendResponse(result)
                }

                override fun onException(e: Throwable) {
                    asyncCtx.sendException(e)
                }
            }, bizCtx.clientTimeout)
        } else {
            clientProxy.oneway(request.request)
        }
    }

    override fun multiInterest(): List<String> {
        return listOf(
            AuthServerRequest::class.java.name,
            DatabaseServerRequest::class.java.name,
            SharedServerRequest::class.java.name,
            GameServerRequest::class.java.name,
            LogServerRequest::class.java.name,
        )
    }

    private fun route(message: RequestMessageContainer): BrokerClientItem? {
        return when (message) {
            is AuthServerRequest -> {
                val name = ClientType.AUTH
                Broker.getClient(name)
            }

            is DatabaseServerRequest -> {
                val name = String.format("%s-%d", ClientType.DB, message.dbId)
                Broker.getClient(name)
            }

            is SharedServerRequest -> {
                val name = ClientType.SHARED
                Broker.getClient(name)
            }

            is GameServerRequest -> {
                val player = Broker.getPlayer(message.uniqueId)
                return player?.server
            }

            is LogServerRequest -> {
                val name = ClientType.LOG
                Broker.getClient(name)
            }

            else -> null
        }
    }
}
