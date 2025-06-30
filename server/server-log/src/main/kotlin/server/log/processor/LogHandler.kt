package server.log.processor

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import server.common.message.log.LogMessage
import server.common.processor.RequestDispatcher
import server.log.schema.LogTable
import server.log.schema.initSchema
import java.util.concurrent.ConcurrentHashMap

class LogHandler : RequestDispatcher.Handler<LogMessage> {

    private val logTables = ConcurrentHashMap<String, LogTable>()

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: LogMessage) {
        transaction {
            val table = logTables.computeIfAbsent(request.tableName) { _ ->
                val fieldTypes = request.fields.mapValues { it.value.javaClass.kotlin }
                val logTable = LogTable(request.tableName, fieldTypes)
                logTable.initSchema()
                logTable
            }
            table.insertLog(request.fields)
        }
    }

    override fun interest(): Class<LogMessage> {
        return LogMessage::class.java
    }
}
