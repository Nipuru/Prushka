package server.log.processor

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import server.common.message.log.ReportErrorMessage
import server.common.processor.RequestDispatcher
import server.log.schema.ServerErrorTable


/**
 * @author Nipuru
 * @since 2025/06/30 18:18
 */
class ReportErrorHandler: RequestDispatcher.Handler<ReportErrorMessage> {
    override fun handle(asyncCtx: RequestDispatcher.ResponseContext, request: ReportErrorMessage) {
        transaction {
            ServerErrorTable.insert {
                it[serverType] = request.serverType
                it[serverName] = request.serverName
                it[errorMessage] = request.errorMessage
                it[stackTrace] = request.stackTrace
                it[time] = request.time
            }
        }
    }

    override fun interest(): Class<ReportErrorMessage> {
        return ReportErrorMessage::class.java
    }
}