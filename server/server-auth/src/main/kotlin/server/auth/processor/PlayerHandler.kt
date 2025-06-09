package server.auth.processor

import server.auth.service.PlayerService
import server.common.processor.RequestDispatcher
import server.common.processor.RequestDispatcher.ResponseContext

class PlayerRequestHandler : RequestDispatcher.Handler<server.common.message.auth.PlayerRequestMessage> {
    override fun handle(asyncCtx: ResponseContext, request: server.common.message.auth.PlayerRequestMessage) {
        val user = PlayerService.initPlayer(request.name, request.uniqueId, request.ip)
        asyncCtx.sendResponse(user)
    }

    override fun interest(): Class<server.common.message.auth.PlayerRequestMessage> {
        return server.common.message.auth.PlayerRequestMessage::class.java
    }
}
