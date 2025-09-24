package server.common.message

import server.common.message.PlayerDataMessage.FieldValue
import java.io.Serializable


/**
 * @author Nipuru
 * @since 2025/09/24 15:52
 */
class PlayerDataTransactionMessage(val playerId: Int) : Serializable {
    val deletes = mutableListOf<Delete>()
    val updates = mutableListOf<Update>()
    val inserts = mutableListOf<Insert>()

    fun addDelete(tableName: String, uniqueFields: List<FieldValue>) {
        deletes.add(Delete(tableName, uniqueFields))
    }

    fun addUpdate(tableName: String, uniqueFields: List<FieldValue>, updateFields: List<FieldValue>) {
        updates.add(Update(tableName, uniqueFields, updateFields))
    }

    fun addInsert(tableName: String, uniqueFields: List<FieldValue>) {
        inserts.add(Insert(tableName, uniqueFields))
    }

    class Delete(val tableName: String, val uniqueFields: List<FieldValue>) : Serializable

    class Insert(val tableName: String, val fields: List<FieldValue>) : Serializable

    class Update(val tableName: String, val uniqueFields: List<FieldValue>, val updateFields: List<FieldValue>) :
        Serializable
}