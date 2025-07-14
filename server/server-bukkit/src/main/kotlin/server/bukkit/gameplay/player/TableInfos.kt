package server.bukkit.gameplay.player

import server.common.message.TableInfo
import java.io.Serializable


/**
 * @author Nipuru
 * @since 2025/06/11 15:04
 */
class TableInfos : Serializable {
    val tables = mutableListOf<TableInfo>()
}
