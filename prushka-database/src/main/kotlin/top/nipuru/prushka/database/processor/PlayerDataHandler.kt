package top.nipuru.prushka.database.processor

import top.nipuru.prushka.common.message.database.FieldMessage
import top.nipuru.prushka.common.message.database.PlayerTransactionRequest
import top.nipuru.prushka.common.message.database.QueryPlayerRequest
import top.nipuru.prushka.common.processor.RequestDispatcher
import top.nipuru.prushka.database.player.PlayerDataManager

class QueryPlayerHandler : RequestDispatcher.Handler<QueryPlayerRequest> {

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: QueryPlayerRequest) {
        val data: Map<String, List<List<FieldMessage>>> = PlayerDataManager.queryPlayer(request)
        asyncCtx.sendResponse(data)
    }

    override fun interest(): Class<QueryPlayerRequest> {
        return QueryPlayerRequest::class.java
    }
}

class PlayerTransactionHandler : RequestDispatcher.Handler<PlayerTransactionRequest> {

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: PlayerTransactionRequest) {
        PlayerDataManager.transaction(request)
        asyncCtx.sendResponse(true) // response
    }

    override fun interest(): Class<PlayerTransactionRequest> {
        return PlayerTransactionRequest::class.java
    }
}

