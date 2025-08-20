package server.bukkit.logger

import net.afyer.afybroker.client.Broker
import server.bukkit.BukkitPlugin
import server.bukkit.time.TimeManager
import server.common.service.LogService

object LogServer {
    const val GET_COMMAND = 1

    private val service = Broker.getService(LogService::class.java)

    fun reportError(error: Throwable) {
        BukkitPlugin.submit {
            service.reportError(
                serverType = Broker.getClientInfo().type,
                serverName = Broker.getClientInfo().name,
                errorMessage = error.message ?: "",
                stackTrace = error.stackTraceToString(),
                time = TimeManager.now
            )
        }
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

    fun logAddReward(playerId: Int, itemType: Int, itemId: Int, amount: Long, way: Int) {
        sendLog(
            "tb_add_reward",
            mapOf(
                "player_id" to playerId,
                "item_type" to itemType,
                "item_id" to itemId,
                "amount" to amount,
                "time" to TimeManager.now,
                "way" to way
            )
        )
    }


    fun logSubtractReward(playerId: Int, rewardType: Int, rewardId: Int, amount: Long, way: Int) {
        sendLog(
            "tb_sub_reward",
            mapOf(
                "player_id" to playerId,
                "reward_type" to rewardType,
                "reward_id" to rewardId,
                "amount" to amount,
                "time" to TimeManager.now,
                "way" to way
            )
        )
    }

    private fun sendLog(tableName: String, fields: Map<String, Any>) {
        BukkitPlugin.submit { service.log(tableName, fields) }
    }
}
