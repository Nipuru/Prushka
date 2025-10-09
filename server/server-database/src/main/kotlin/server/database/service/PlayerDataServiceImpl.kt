package server.database.service

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.transaction
import server.common.logger.Logger
import server.common.message.PlayerDataMessage.TableInfo
import server.common.message.PlayerDataTransactionMessage
import server.common.service.PlayerDataService
import server.common.util.database.initSchema
import server.database.schema.PlayerDataTable
import java.util.concurrent.ConcurrentHashMap
import org.jetbrains.exposed.sql.Transaction as ETransaction

class PlayerDataServiceImpl : PlayerDataService {
    private val tableInitialized = ConcurrentHashMap<String, PlayerDataTable>()

    private val sqlLogger = object : SqlLogger {
        override fun log(context: StatementContext, transaction: ETransaction) {
            Logger.info("SQL: ${context.expandArgs(transaction)}")
        }
    }

    override fun queryPlayer(playerId: Int, tables: List<TableInfo>): Map<String, List<Any>> {
        return transaction {
            val result = mutableMapOf<String, MutableList<Any>>()
            for (tableInfo in tables) {
                val table = getTable(tableInfo)
                val values = mutableListOf<Any>(tableInfo.fields.joinToString(";") { it.first })
                table.selectAll().where(table.playerId eq playerId).forEach {
                    for (field in tableInfo.fields) {
                        values.add(table.getColumn(it, field.first))
                    }
                }
                result[tableInfo.tableName] = values
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
