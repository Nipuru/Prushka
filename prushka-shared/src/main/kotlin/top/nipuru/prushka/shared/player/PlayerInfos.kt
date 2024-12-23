package top.nipuru.prushka.shared.player

import org.jetbrains.exposed.sql.Table


/**
 * @author Nipuru
 * @since 2024/12/18 14:23
 */
object PlayerInfos : Table() {
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
