package top.nipuru.minegame.common.message.database

import java.io.Serializable

class FieldMessage(val name: String, val value: Any) : Serializable

class PlayerTransactionRequest(val playerId: Int) : Serializable {
    val deletes = mutableListOf<Delete>()
    val updates = mutableListOf<Update>()
    val inserts = mutableListOf<Insert>()
}

class Delete(val tableName: String, val uniqueFields: List<FieldMessage>) : Serializable

class Insert(val tableName: String, val fields: List<FieldMessage>) : Serializable

class Update(val tableName: String, val uniqueFields: List<FieldMessage>, val updateFields: List<FieldMessage>) : Serializable

class QueryPlayerRequest(val playerId: Int) : Serializable {
    val tables = mutableListOf<TableInfo>()
}

class TableInfo(
    val tableName: String,
    val autoCreate: Boolean,
    val fields: Map<String, Class<*>>,
    val uniqueKeys: List<String>
) : Serializable
