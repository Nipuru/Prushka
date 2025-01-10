package top.nipuru.prushka.database.player

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import top.nipuru.prushka.common.message.database.FieldMessage
import top.nipuru.prushka.common.message.database.PlayerDataTransactionMessage
import top.nipuru.prushka.common.message.database.PlayerDataRequestMessage
import top.nipuru.prushka.common.message.database.TableInfo
import top.nipuru.prushka.database.schema.PlayerDatas
import java.util.concurrent.ConcurrentHashMap

object PlayerDataManager {
    private val tableInitialized = ConcurrentHashMap<String, PlayerDatas>()

    fun queryPlayer(request: PlayerDataRequestMessage): Map<String, List<List<FieldMessage>>> {
        return transaction {
            val result = mutableMapOf<String, List<List<FieldMessage>>>()
            for (tableInfo in request.tables) {
                val table = getTable(tableInfo)
                val lists = mutableListOf<List<FieldMessage>>()
                table.selectAll().where(table.playerId eq request.playerId).forEach {
                    val fields = mutableListOf<FieldMessage>()
                    for (fieldName in tableInfo.fields.keys) {
                        val field = FieldMessage(fieldName, table.getColumn(it, fieldName))
                        fields.add(field)
                    }
                    lists.add(fields)
                }
                result[tableInfo.tableName] = lists
            }
            result
        }
    }


    fun transaction(request: PlayerDataTransactionMessage) {
        transaction {
            addLogger(Slf4jSqlDebugLogger)
            for (delete in request.deletes) {
                val table = tableInitialized[delete.tableName]!!
                table.deleteWhere {
                    var condition = table.playerId eq request.playerId
                    for (field in delete.uniqueFields) {
                        val column = table.column(field.name)
                        condition = condition and (column eq field.value)
                    }
                    condition
                }
            }

            for (update in request.updates) {
                val table = tableInitialized[update.tableName]!!
                table.update({
                    var condition = table.playerId eq request.playerId
                    for (uniqueField in update.uniqueFields) {
                        val column = table.column(uniqueField.name)
                        condition = condition and (column eq uniqueField.value)
                    }
                    condition
                }) {
                    for (updateField in update.updateFields) {
                        table.setColumn(it, updateField.name, updateField.value)
                    }
                }
            }

            for (insert in request.inserts) {
                val table = tableInitialized[insert.tableName]!!
                table.insert {
                    for (field in insert.fields) {
                        table.setColumn(it, field.name, field.value)
                    }
                }
            }
        }
    }

    private fun getTable(tableInfo: TableInfo) : PlayerDatas {
        val tables = tableInitialized
        var table = tables[tableInfo.tableName]
        if (table == null) {
            table = PlayerDatas(tableInfo)
            if (tableInfo.autoCreate) {
                SchemaUtils.create(table)
                SchemaUtils.createMissingTablesAndColumns(table)
            }
        }
        return tables.putIfAbsent(tableInfo.tableName, table)!!
    }
}
