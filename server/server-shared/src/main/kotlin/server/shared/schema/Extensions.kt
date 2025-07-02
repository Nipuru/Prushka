package server.shared.schema

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * @author Nipuru
 * @since 2025/06/11 17:22
 */


fun Table.initSchema() {
    transaction {
        SchemaUtils.create(this@initSchema)
        SchemaUtils.createMissingTablesAndColumns(this@initSchema)
    }
}