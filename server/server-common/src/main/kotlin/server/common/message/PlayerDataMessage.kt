package server.common.message

import java.io.Serializable

class FieldMessage(val name: String, val value: Any) : Serializable
data class PlayerDataMessage(val playerId: Int, val dbId: Int, val data: MutableMap<String, MutableList<List<FieldMessage>>>) : Serializable

class TableInfo(val tableName: String, val autoCreate: Boolean, val fields: List<Triple<String, Class<*>, Any>>, val uniqueKeys: List<String>) : Serializable

class PlayerDataTransactionMessage(val playerId: Int) : Serializable {
    val deletes = mutableListOf<Delete>()
    val updates = mutableListOf<Update>()
    val inserts = mutableListOf<Insert>()

    fun addDelete(tableName: String, uniqueFields: List<FieldMessage>) {
        deletes.add(Delete(tableName, uniqueFields))
    }

    fun addUpdate(tableName: String, uniqueFields: List<FieldMessage>, updateFields: List<FieldMessage>) {
        updates.add(Update(tableName, uniqueFields, updateFields))
    }

    fun addInsert(tableName: String, uniqueFields: List<FieldMessage>) {
        inserts.add(Insert(tableName, uniqueFields))
    }

    class Delete(val tableName: String, val uniqueFields: List<FieldMessage>) : Serializable

    class Insert(val tableName: String, val fields: List<FieldMessage>) : Serializable

    class Update(val tableName: String, val uniqueFields: List<FieldMessage>, val updateFields: List<FieldMessage>) :
        Serializable
}
