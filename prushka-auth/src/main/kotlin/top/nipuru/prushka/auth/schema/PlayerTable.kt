package top.nipuru.prushka.auth.schema

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Sequence


/**
 * @author Nipuru
 * @since 2024/12/18 11:25
 */
object PlayerTable : Table() {
    override val tableName = "tb_player"
    val playerId = integer("player_id").autoIncrement(Sequence("player_id_seq", startWith = 100000))
    val name = varchar("name", 16)
    val uniqueId = varchar("unique_id", 32).uniqueIndex()
    val lastIp = text("last_ip")
    val dbId = integer("db_id")
    val createTime = long("create_time")
    override val primaryKey = PrimaryKey(playerId)
}