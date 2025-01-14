package top.nipuru.prushka.shared.schema

import org.jetbrains.exposed.sql.Table


/**
 * @author Nipuru
 * @since 2024/12/18 14:23
 */
object PlayerInfoTable : Table() {
    override val tableName = "tb_player_info"
    val playerId = integer("player_id")
    val name = varchar("name", 16).uniqueIndex()
    val dbId = integer("db_id")
    val coin = long("coin")
    val rankId = integer("rank_id")
    val createTime = long("create_time")
    val logoutTime = long("logout_time")
    val playedTime = long("played_time")
    val texture = array<String>("texture")
    override val primaryKey = PrimaryKey(playerId)
}
