package server.auth.processor

import server.auth.service.PlayerService
import server.common.message.auth.PlayerLoginRequest
import server.common.processor.RequestDispatcher
import server.common.processor.RequestDispatcher.ResponseContext

class PlayerLoginHandler : RequestDispatcher.Handler<PlayerLoginRequest> {
    override fun handle(asyncCtx: ResponseContext, request: PlayerLoginRequest) {
        val user = PlayerService.initPlayer(request.name, request.uniqueId, request.ip)
        asyncCtx.sendResponse(user)
    }

    override fun interest(): Class<PlayerLoginRequest> {
        return PlayerLoginRequest::class.java
    }
}
