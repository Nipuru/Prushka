package server.log.schema

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction


/**
 * @author Nipuru
 * @since 2025/06/30 18:18
 */
object ServerErrorTable : Table() {
    override val tableName: String = "tb_server_error"
    val serverType = text("server_type")
    val serverName = text("server_name")
    val errorMessage = text("error_message")
    val stackTrace = text("stack_trace")
    val time = long("time")

    init {
        initSchema()
    }
}
