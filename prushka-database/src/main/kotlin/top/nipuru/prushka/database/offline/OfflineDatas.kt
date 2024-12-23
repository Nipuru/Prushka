package top.nipuru.prushka.database.offline

import org.jetbrains.exposed.sql.Table


/**
 * @author Nipuru
 * @since 2024/12/18 15:12
 */
object OfflineDatas : Table() {
    override val tableName = "tb_offline_data"
    val id = long("id").autoIncrement()
    val playerId = integer("player_id").index()
    val module = text("module")
    val data = text("data")
    override val primaryKey = PrimaryKey(id)
}
