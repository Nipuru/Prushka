package server.common.message.database

import java.io.Serializable


/**
 * @author Nipuru
 * @since 2025/06/11 15:02
 */
class PlayerDataTransactionRequest(val playerId: Int) : Serializable {
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

    class Update(val tableName: String, val uniqueFields: List<FieldMessage>, val updateFields: List<FieldMessage>) : Serializable
}
