package server.database.processor

import server.common.message.database.FieldMessage
import server.common.message.database.PlayerDataQueryRequest
import server.common.message.database.PlayerDataTransactionRequest
import server.common.processor.RequestDispatcher
import server.database.service.PlayerDataService

class PlayerDataQueryHandler : RequestDispatcher.Handler<PlayerDataQueryRequest> {

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: PlayerDataQueryRequest) {
        val data: Map<String, List<List<FieldMessage>>> = PlayerDataService.queryPlayer(request)
        asyncCtx.sendResponse(data)
    }

    override fun interest(): Class<PlayerDataQueryRequest> {
        return PlayerDataQueryRequest::class.java
    }
}

class PlayerDataTransactionHandler : RequestDispatcher.Handler<PlayerDataTransactionRequest> {

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: PlayerDataTransactionRequest) {
        PlayerDataService.transaction(request)
        asyncCtx.sendResponse(true) // response
    }

    override fun interest(): Class<PlayerDataTransactionRequest> {
        return PlayerDataTransactionRequest::class.java
    }
}

