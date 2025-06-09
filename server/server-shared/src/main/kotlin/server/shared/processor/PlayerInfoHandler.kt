package server.shared.processor

import server.common.message.shared.GetPlayerInfoRequest
import server.common.message.shared.GetPlayerInfosRequest
import server.common.message.shared.PlayerInfoMessage
import server.common.message.shared.PlayerInfoUpdateNotify
import server.common.processor.RequestDispatcher
import server.shared.service.PlayerInfoService

class GetPlayerInfoHandler : RequestDispatcher.Handler<GetPlayerInfoRequest> {
    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: GetPlayerInfoRequest) {
        val playerInfo: PlayerInfoMessage? = PlayerInfoService.getByName(request.name)
        asyncCtx.sendResponse(playerInfo)
    }

    override fun interest(): Class<GetPlayerInfoRequest> {
        return GetPlayerInfoRequest::class.java
    }
}

class GetPlayerInfosHandler : RequestDispatcher.Handler<GetPlayerInfosRequest> {
    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: GetPlayerInfosRequest) {
        val playerInfos: Map<Int, PlayerInfoMessage> = PlayerInfoService.getByIds(request.playerIds)
        asyncCtx.sendResponse(playerInfos)
    }

    override fun interest(): Class<GetPlayerInfosRequest> {
        return GetPlayerInfosRequest::class.java
    }
}

class PlayerInfoUpdateHandler : RequestDispatcher.Handler<PlayerInfoUpdateNotify> {
    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: PlayerInfoUpdateNotify) {
        PlayerInfoService.insertOrUpdate(request.playerInfo)
    }

    override fun interest(): Class<PlayerInfoUpdateNotify> {
        return PlayerInfoUpdateNotify::class.java
    }
}
