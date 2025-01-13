package top.nipuru.prushka.game.logger

import top.nipuru.prushka.common.message.log.LogMessage
import top.nipuru.prushka.game.route.logNotify
import top.nipuru.prushka.game.util.submit

object LogServer {
    const val GET_COMMAND = 1

    fun logAddItem(playerId: Int, itemType: Int, itemId: Int, amount: Long, way: Int) {
        sendLog(LogMessage("tb_add_item", mapOf(
            "player_id" to playerId,
            "item_type" to itemType,
            "item_id" to itemId,
            "amount" to amount,
            "way" to way
        )))
    }


    fun logSubtractItem(playerId: Int, itemType: Int, itemId: Int, amount: Long, way: Int) {
        sendLog(LogMessage("tb_sub_item", mapOf(
            "player_id" to playerId,
            "item_type" to itemType,
            "item_id" to itemId,
            "amount" to amount,
            "way" to way
        )))
    }

    private fun sendLog(logMessage: LogMessage) {
        submit { logNotify(logMessage) }
    }
}
