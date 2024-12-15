package top.nipuru.minegame.auth.processor

import top.nipuru.minegame.auth.user.UserManager
import top.nipuru.minegame.common.message.auth.QueryUserRequest
import top.nipuru.minegame.common.message.auth.UserMessage
import top.nipuru.minegame.common.processor.RequestDispatcher
import top.nipuru.minegame.common.processor.RequestDispatcher.ResponseContext

class QueryUserHandler : RequestDispatcher.Handler<QueryUserRequest> {
    override fun handle(asyncCtx: ResponseContext, request: QueryUserRequest) {
        val user = UserManager.initUser(request.name, request.uniqueId, request.ip)
        val userMessage = UserMessage(user.playerId, user.dbId)
        asyncCtx.sendResponse(userMessage)
    }

    override fun interest(): Class<QueryUserRequest> {
        return QueryUserRequest::class.java
    }
}
