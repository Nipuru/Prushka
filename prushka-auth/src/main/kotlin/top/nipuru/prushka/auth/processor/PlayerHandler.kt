package top.nipuru.prushka.auth.processor

import top.nipuru.prushka.auth.service.PlayerService
import top.nipuru.prushka.common.message.auth.PlayerRequestMessage
import top.nipuru.prushka.common.processor.RequestDispatcher
import top.nipuru.prushka.common.processor.RequestDispatcher.ResponseContext

class PlayerRequestHandler : RequestDispatcher.Handler<PlayerRequestMessage> {
    override fun handle(asyncCtx: ResponseContext, request: PlayerRequestMessage) {
        val user = PlayerService.initPlayer(request.name, request.uniqueId, request.ip)
        asyncCtx.sendResponse(user)
    }

    override fun interest(): Class<PlayerRequestMessage> {
        return PlayerRequestMessage::class.java
    }
}
