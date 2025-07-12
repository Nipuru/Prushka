package server.common.service

import server.common.message.FieldMessage
import java.io.Serializable


/**
 * @author Nipuru
 * @since 2025/07/12 17:49
 */
interface PlayerDataService {
    fun queryPlayer(playerId: Int, tables: List<TableInfo>): MutableMap<String, MutableList<List<FieldMessage>>>

    fun transaction(request: Transaction)

    class TableInfo(val tableName: String, val autoCreate: Boolean, val fields: Map<String, Class<*>>, val uniqueKeys: List<String>) : Serializable

    class Transaction(val playerId: Int) : Serializable {
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
}