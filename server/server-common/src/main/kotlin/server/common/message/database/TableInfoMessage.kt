package server.common.message.database

import java.io.Serializable


/**
 * @author Nipuru
 * @since 2025/06/11 15:05
 */
class TableInfoMessage(
    val tableName: String,
    val autoCreate: Boolean,
    val fields: Map<String, Class<*>>,
    val uniqueKeys: List<String>
) : Serializable