package top.nipuru.prushka.database.processor

import top.nipuru.prushka.common.message.database.FieldMessage
import top.nipuru.prushka.common.message.database.PlayerDataTransactionMessage
import top.nipuru.prushka.common.message.database.PlayerDataRequestMessage
import top.nipuru.prushka.common.processor.RequestDispatcher
import top.nipuru.prushka.database.service.PlayerDataService

class PlayerDataRequestHandler : RequestDispatcher.Handler<PlayerDataRequestMessage> {

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: PlayerDataRequestMessage) {
        val data: Map<String, List<List<FieldMessage>>> = PlayerDataService.queryPlayer(request)
        asyncCtx.sendResponse(data)
    }

    override fun interest(): Class<PlayerDataRequestMessage> {
        return PlayerDataRequestMessage::class.java
    }
}

class PlayerDataTransactionHandler : RequestDispatcher.Handler<PlayerDataTransactionMessage> {

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: PlayerDataTransactionMessage) {
        PlayerDataService.transaction(request)
        asyncCtx.sendResponse(true) // response
    }

    override fun interest(): Class<PlayerDataTransactionMessage> {
        return PlayerDataTransactionMessage::class.java
    }
}

