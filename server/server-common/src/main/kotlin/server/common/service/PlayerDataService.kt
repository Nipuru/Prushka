package server.common.service

import server.common.message.PlayerDataMessage.TableInfo
import server.common.message.PlayerDataTransactionMessage


/**
 * @author Nipuru
 * @since 2025/07/12 17:49
 */
interface PlayerDataService {
    fun queryPlayer(playerId: Int, tables: List<TableInfo>): Map<String, List<Any>>

    fun transaction(request: PlayerDataTransactionMessage)
}