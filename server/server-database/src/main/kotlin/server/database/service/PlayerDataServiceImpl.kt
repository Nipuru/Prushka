package server.database.service

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.transaction
import server.common.logger.Logger
import server.common.message.FieldMessage
import server.common.message.PlayerDataTransactionMessage
import server.common.message.TableInfo
import server.common.service.PlayerDataService
import server.database.schema.PlayerDataTable
import server.database.schema.initSchema
import java.util.concurrent.ConcurrentHashMap
import org.jetbrains.exposed.sql.Transaction as ETransaction

class PlayerDataServiceImpl : PlayerDataService {
    private val tableInitialized = ConcurrentHashMap<String, PlayerDataTable>()

    private val sqlLogger = object : SqlLogger {
        override fun log(context: StatementContext, transaction: ETransaction) {
            Logger.info("SQL: ${context.expandArgs(transaction)}")
        }
    }

    override fun queryPlayer(playerId: Int, tables: List<TableInfo>): MutableMap<String, MutableList<List<FieldMessage>>> {
        return transaction {
            val result = mutableMapOf<String, MutableList<List<FieldMessage>>>()
            for (tableInfo in tables) {
                val table = getTable(tableInfo)
                val lists = mutableListOf<List<FieldMessage>>()
                if (table.exists()) {
                    table.selectAll().where(table.playerId eq playerId).forEach {
                        val fields = mutableListOf<FieldMessage>()
                        for (fieldName in tableInfo.fields.keys) {
                            val field = FieldMessage(fieldName, table.getColumn(it, fieldName))
                            fields.add(field)
                        }
                        lists.add(fields)
                    }
                }
                result[tableInfo.tableName] = lists
            }
            result
        }
    }


    override fun transaction(request: PlayerDataTransactionMessage) {
        transaction {
            addLogger(sqlLogger)
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
                    it[playerId] = request.playerId
                    for (field in insert.fields) {
                        table.setColumn(it, field.name, field.value)
                    }
                }
            }
        }
    }

    private fun getTable(tableInfo: TableInfo) : PlayerDataTable {
        val tables = tableInitialized
        var table = tables[tableInfo.tableName]
        if (table == null) {
            table = PlayerDataTable(tableInfo)
            if (tableInfo.autoCreate) {
                table.initSchema()
            }
        }
        return tables.putIfAbsent(tableInfo.tableName, table) ?: table
    }
}
