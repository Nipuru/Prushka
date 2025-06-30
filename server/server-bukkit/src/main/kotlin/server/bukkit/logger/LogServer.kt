package server.bukkit.logger

import net.afyer.afybroker.client.Broker
import server.bukkit.route.logNotify
import server.bukkit.time.TimeManager
import server.bukkit.util.submit
import server.common.message.log.LogMessage
import server.common.message.log.ReportErrorMessage

object LogServer {
    const val GET_COMMAND = 1

    fun reportError(error: Throwable) {
        val message = ReportErrorMessage(
            serverType = Broker.getClientInfo().type,
            serverName = Broker.getClientInfo().name,
            errorMessage = error.message ?: "",
            stackTrace = error.stackTraceToString(),
            time = TimeManager.now
        )
        submit { logNotify(message) }
    }


    fun logRegister(playerId: Int) {
        sendLog("tb_register", mapOf("player_id" to playerId, "time" to TimeManager.now))
    }

    fun logLogin(playerId: Int, address: String) {
        sendLog("tb_login", mapOf("player_id" to playerId, "address" to address, "time" to TimeManager.now))
    }

    fun logLogout(playerId: Int, address: String) {
        sendLog("tb_logout", mapOf("player_id" to playerId, "address" to address, "time" to TimeManager.now))
    }

    fun logAddItem(playerId: Int, itemType: Int, itemId: Int, amount: Long, way: Int) {
        sendLog("tb_add_item", mapOf("player_id" to playerId, "item_type" to itemType, "item_id" to itemId, "amount" to amount, "time" to TimeManager.now, "way" to way))
    }


    fun logSubtractItem(playerId: Int, itemType: Int, itemId: Int, amount: Long, way: Int) {
        sendLog("tb_sub_item", mapOf("player_id" to playerId, "item_type" to itemType, "item_id" to itemId, "amount" to amount, "time" to TimeManager.now, "way" to way))
    }

    private fun sendLog(tableName: String, fields: Map<String, Any>) {
        submit { logNotify(LogMessage(tableName, fields)) }
    }
}
