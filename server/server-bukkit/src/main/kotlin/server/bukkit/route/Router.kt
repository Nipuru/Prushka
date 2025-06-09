package server.bukkit.route

import net.afyer.afybroker.client.Broker
import server.common.message.*


/**
 * 对消息进行一层封装 使得消息只在 brokerClient 完成序列化和反序列化
 * 一些逻辑简单的 转发类的消息可以考虑使用此方法
 * [server.common.processor.RequestDispatcher]
 */

fun <T> sharedRequest(request: Any): T? {
    val requestMessage = SharedServerRequest(createRequest(request))
    val responseMessage = Broker.invokeSync<ResponseMessage>(requestMessage)
    return responseMessage.getData()
}

fun sharedNotify(request: Any) {
    val requestMessage = SharedServerRequest(createRequest(request))
    Broker.oneway(requestMessage)
}

fun <T> authRequest(request: Any): T? {
    val requestMessage = AuthServerRequest(createRequest(request))
    val responseMessage = Broker.invokeSync<ResponseMessage>(requestMessage)
    return responseMessage.getData()
}

fun authNotify(request: Any) {
    val requestMessage = AuthServerRequest(createRequest(request))
    Broker.oneway(requestMessage)
}

fun <T> databaseRequest(dbId: Int, request: Any): T? {
    val requestMessage = DatabaseServerRequest(dbId, createRequest(request))
    val responseMessage = Broker.invokeSync<ResponseMessage>(requestMessage)
    return responseMessage.getData()
}

fun databaseNotify(dbId: Int, request: Any) {
    val requestMessage = DatabaseServerRequest(dbId, createRequest(request))
    Broker.oneway(requestMessage)
}

fun logNotify(request: Any) {
    val requestMessage = LogServerRequest(createRequest(request))
    Broker.oneway(requestMessage)
}