package top.nipuru.minegame.shared.processor

import top.nipuru.minegame.common.message.shared.PlayerInfoMessage
import top.nipuru.minegame.common.message.shared.PlayerInfoUpdateNotify
import top.nipuru.minegame.common.message.shared.GetPlayerInfoRequest
import top.nipuru.minegame.common.message.shared.GetPlayerInfosRequest
import top.nipuru.minegame.common.processor.RequestDispatcher
import top.nipuru.minegame.shared.player.PlayerInfoManager

class GetPlayerInfoHandler : RequestDispatcher.Handler<GetPlayerInfoRequest> {
    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: GetPlayerInfoRequest) {
        val playerInfo: PlayerInfoMessage? = PlayerInfoManager.getByName(request.name)
        asyncCtx.sendResponse(playerInfo)
    }

    override fun interest(): Class<GetPlayerInfoRequest> {
        return GetPlayerInfoRequest::class.java
    }
}

class GetPlayerInfosHandler : RequestDispatcher.Handler<GetPlayerInfosRequest> {
    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: GetPlayerInfosRequest) {
        val playerInfos: Map<Int, PlayerInfoMessage> = PlayerInfoManager.getByIds(request.playerIds)
        asyncCtx.sendResponse(playerInfos)
    }

    override fun interest(): Class<GetPlayerInfosRequest> {
        return GetPlayerInfosRequest::class.java
    }
}

class PlayerInfoUpdateHandler : RequestDispatcher.Handler<PlayerInfoUpdateNotify> {
    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: PlayerInfoUpdateNotify) {
        PlayerInfoManager.insertOrUpdate(request.playerInfo)
    }

    override fun interest(): Class<PlayerInfoUpdateNotify> {
        return PlayerInfoUpdateNotify::class.java
    }
}
