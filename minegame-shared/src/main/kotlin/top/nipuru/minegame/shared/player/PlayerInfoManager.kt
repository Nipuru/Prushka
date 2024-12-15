package top.nipuru.minegame.shared.player

import top.nipuru.minegame.common.message.shared.PlayerInfoMessage
import top.nipuru.minegame.shared.dataSource
import java.util.concurrent.ConcurrentHashMap

object PlayerInfoManager {
    private val createTable = """
            CREATE TABLE IF NOT EXISTS tb_player_info(
                player_id        INTEGER     NOT NULL,
                name             VARCHAR(16) NOT NULL,
                db_id            INTEGER     NOT NULL,
                coin             BIGINT      NOT NULL,
                rank_id          INTEGER     NOT NULL,
                create_time      BIGINT      NOT NULL,
                logout_time      BIGINT      NOT NULL,
                played_time      BIGINT      NOT NULL,
                texture          TEXT[]      NOT NULL,
                CONSTRAINT pkey_tb_player_info    PRIMARY KEY (player_id),
                CONSTRAINT uni_tb_player_info     UNIQUE (name)
              );
            
            """.trimIndent()

    private const val selectByIds =
        "SELECT player_id,name,db_id,coin,rank_id,create_time,logout_time,played_time,texture from tb_player_info WHERE player_id in (%s);"

    private const val selectByName =
        "SELECT player_id,name,db_id,coin,rank_id,create_time,logout_time,played_time,texture from tb_player_info WHERE name = ?;"

    private const val insertOfUpdate =
        "INSERT INTO tb_player_info(player_id,name,db_id,coin,rank_id,create_time,logout_time,played_time,texture) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (player_id) DO UPDATE SET " +
                "name=EXCLUDED.name, " +
                "coin=EXCLUDED.coin, " +
                "db_id=EXCLUDED.db_id, " +
                "rank_id=EXCLUDED.rank_id, " +
                "create_time=EXCLUDED.create_time, " +
                "logout_time=EXCLUDED.logout_time, " +
                "played_time=EXCLUDED.played_time, " +
                "texture=EXCLUDED.texture "

    private val byId = ConcurrentHashMap<Int, PlayerInfoMessage>()
    private val byName = ConcurrentHashMap<String, PlayerInfoMessage>()

    fun init() {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute(
                    createTable
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getByIds(playerIds: List<Int>): Map<Int, PlayerInfoMessage> {
        val result = mutableMapOf<Int, PlayerInfoMessage>()
        val query = mutableListOf<Int>()
        for (playerId in playerIds) {
            if (byId.contains(playerId)) {
                result[playerId] = byId[playerId]!!
            } else {
                query.add(playerId)
            }
        }
        if (query.isEmpty()) {
            return result
        }
        val querySql = selectByIds.format(query.joinToString(","))
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery(querySql).use { rs ->
                    while (rs.next()) {
                        PlayerInfoMessage().also {
                            it.playerId = rs.getInt(1)
                            it.name = rs.getString(2)
                            it.dbId = rs.getInt(3)
                            it.coin = rs.getLong(4)
                            it.rankId = rs.getInt(5)
                            it.createTime = rs.getLong(6)
                            it.logoutTime = rs.getLong(7)
                            it.playedTime = rs.getLong(8)
                            it.texture = (rs.getArray(9).array as Array<String>)

                            byId[it.playerId] = it
                            byName[it.name] = it
                            result[it.playerId] = it
                        }
                    }
                }
            }
        }
        return result
    }

    @Suppress("UNCHECKED_CAST")
    fun getByName(name: String): PlayerInfoMessage? {
        if (byName.contains(name)) {
            return byName[name]
        }

        dataSource.connection.use { conn ->
            conn.prepareStatement(selectByName).use { ps ->
                ps.setString(1, name)
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        PlayerInfoMessage().also {
                            it.playerId = rs.getInt(1)
                            it.name = rs.getString(2)
                            it.dbId = rs.getInt(3)
                            it.coin = rs.getLong(4)
                            it.rankId = rs.getInt(5)
                            it.createTime = rs.getLong(6)
                            it.logoutTime = rs.getLong(7)
                            it.playedTime = rs.getLong(8)
                            it.texture = (rs.getArray(9).array as Array<String>)

                            byId[it.playerId] = it
                            byName[it.name] = it
                            return it
                        }
                    }
                }
            }
        }
        return null
    }

    fun insertOrUpdate(playerInfo: PlayerInfoMessage) {
        // 更新数据库
        dataSource.connection.use { conn ->
            conn.prepareStatement(insertOfUpdate).use { ps ->
                ps.setInt(1, playerInfo.playerId)
                ps.setString(2, playerInfo.name)
                ps.setInt(3, playerInfo.dbId)
                ps.setLong(4, playerInfo.coin)
                ps.setInt(5, playerInfo.rankId)
                ps.setLong(6, playerInfo.createTime)
                ps.setLong(7, playerInfo.logoutTime)
                ps.setLong(8, playerInfo.playedTime)
                ps.setObject(9, playerInfo.texture)
                ps.executeUpdate()
            }
        }
        byId[playerInfo.playerId] = playerInfo
        byName[playerInfo.name] = playerInfo
    }
}
