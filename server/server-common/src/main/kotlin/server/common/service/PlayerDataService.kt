package server.common.service

import server.common.message.FieldMessage
import server.common.message.PlayerDataTransactionMessage
import server.common.message.TableInfo
import java.io.Serializable


/**
 * @author Nipuru
 * @since 2025/07/12 17:49
 */
interface PlayerDataService {
    fun queryPlayer(playerId: Int, tables: List<TableInfo>): MutableMap<String, MutableList<List<FieldMessage>>>

    fun transaction(request: PlayerDataTransactionMessage)
}