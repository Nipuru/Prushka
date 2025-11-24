package server.database.schema

import org.jetbrains.exposed.sql.Table
import server.common.util.database.initSchema


/**
 * @author Nipuru
 * @since 2024/12/18 15:12
 */
object OfflineDataTable : Table() {
    override val tableName = "tb_offline"
    val id = long("id").autoIncrement()
    val playerId = integer("player_id").index()
    val module = text("module")
    val data = text("data")
    val duplicateKey = text("duplicate_key").default("")
    override val primaryKey = PrimaryKey(id)

    init {
        initSchema()
    }
}
