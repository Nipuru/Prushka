package server.auth.schema

import org.jetbrains.exposed.sql.Table


/**
 * @author Nipuru
 * @since 2025/01/10 16:27
 */
object AdminUserTable : Table() {
    override val tableName = "tb_admin_user"
    val userId = integer("user_id").autoIncrement()
    val username = varchar("name", 16).uniqueIndex()
    val password = varchar("password", 32)
    val enabled = bool("enabled")
    override val primaryKey = PrimaryKey(userId)
}