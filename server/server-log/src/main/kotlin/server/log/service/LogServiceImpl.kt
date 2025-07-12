package server.log.service

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import server.common.service.LogService
import server.log.schema.LogTable
import server.log.schema.ServerErrorTable
import server.log.schema.initSchema
import java.util.concurrent.ConcurrentHashMap

class LogServiceImpl : LogService {

    private val logTables = ConcurrentHashMap<String, LogTable>()


    override fun log(tableName: String, fields: Map<String, Any>) {
        transaction {
            val table = logTables.computeIfAbsent(tableName) { _ ->
                val fieldTypes = fields.mapValues { it.value.javaClass.kotlin }
                val logTable = LogTable(tableName, fieldTypes)
                logTable.initSchema()
                logTable
            }
            table.insertLog(fields)
        }
    }

    override fun reportError(serverType: String, serverName: String, errorMessage: String, stackTrace: String, time: Long) {
        transaction {
            ServerErrorTable.insert {
                it[ServerErrorTable.serverType] = serverType
                it[ServerErrorTable.serverName] = serverName
                it[ServerErrorTable.errorMessage] = errorMessage
                it[ServerErrorTable.stackTrace] = stackTrace
                it[ServerErrorTable.time] = time
            }
        }
    }
}
