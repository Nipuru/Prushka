package top.nipuru.prushka.log.processor

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import top.nipuru.prushka.common.message.log.LogMessage
import top.nipuru.prushka.common.processor.RequestDispatcher
import top.nipuru.prushka.log.schema.LogTable
import java.util.concurrent.ConcurrentHashMap

class LogHandler : RequestDispatcher.Handler<LogMessage> {

    private val logTables = ConcurrentHashMap<String, LogTable>()

    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: LogMessage) {
        transaction {
            val table = logTables.computeIfAbsent(request.tableName) { _ ->
                val fieldTypes = request.fields.mapValues { it.value.javaClass.kotlin }
                val logTable = LogTable(request.tableName, fieldTypes)
                SchemaUtils.create(logTable)
                SchemaUtils.createMissingTablesAndColumns(logTable)
                logTable
            }
            table.insertLog(request.fields)
        }
    }

    override fun interest(): Class<LogMessage> {
        return LogMessage::class.java
    }
}
