package server.auth.schema

import org.jetbrains.exposed.sql.Sequence
import org.jetbrains.exposed.sql.Table


/**
 * @author Nipuru
 * @since 2024/12/18 11:25
 */
object PlayerTable : Table() {
    override val tableName = "tb_player"
    val playerId = integer("player_id").autoIncrement(Sequence("player_id_seq", startWith = 100000))
    val name = varchar("name", 16)
    val uniqueId = varchar("unique_id", 36).uniqueIndex()
    val lastIp = text("last_ip")
    val dbId = integer("db_id")
    val createTime = long("create_time")
    override val primaryKey = PrimaryKey(playerId)
}