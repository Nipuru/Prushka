package server.database.logger

import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * @author Nipuru
 * @since 2024/11/28 15:13
 */
val logger: Logger = LoggerFactory.getLogger("Prushka")

val sqlLogger = object : SqlLogger {
    override fun log(context: StatementContext, transaction: Transaction) {
        logger.info("SQL: ${context.expandArgs(transaction)}")
    }
}