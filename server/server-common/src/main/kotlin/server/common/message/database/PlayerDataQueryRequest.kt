package server.common.message.database

import java.io.Serializable


/**
 * @author Nipuru
 * @since 2025/06/11 15:04
 */
class PlayerDataQueryRequest(val playerId: Int) : Serializable {
    val tables = mutableListOf<TableInfoMessage>()
}
