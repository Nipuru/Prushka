package server.common.message

import java.io.Serializable

data class PlayerDataMessage(val playerId: Int, val dbId: Int, val data: MutableMap<String, MutableList<List<FieldValue>>>) : Serializable {
    class FieldValue(val name: String, val value: Any) : Serializable
    class TableInfo(val tableName: String, val autoCreate: Boolean, val fields: List<Triple<String, Class<*>, Any>>, val uniqueKeys: List<String>) : Serializable
}
