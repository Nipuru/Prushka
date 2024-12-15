package top.nipuru.minegame.auth.user

import top.nipuru.minegame.auth.dataSource
import java.util.*


/**
 * @author Nipuru
 * @since 2024/11/07 17:28
 */
object UserManager {

    private val createSequence = "CREATE SEQUENCE IF NOT EXISTS tb_user_player_id_seq START 100000;"

    private val createTable = """
            CREATE TABLE IF NOT EXISTS tb_user (
                player_id   INTEGER      NOT NULL DEFAULT nextval('tb_user_player_id_seq'),
                name        VARCHAR(16)  NOT NULL,
                unique_id   VARCHAR(36)  NOT NULL,
                last_ip     TEXT         NOT NULL,
                db_id       INTEGER      NOT NULL,
                create_time BIGINT       NOT NULL,
                CONSTRAINT  pkey_tb_user PRIMARY KEY (player_id),
                CONSTRAINT  uni_tb_user  UNIQUE (unique_id)
            );
            """.trimIndent()

    private const val select = "SELECT player_id,name,unique_id,last_ip,db_id,create_time FROM tb_user where unique_id=?;"

    private const val insertOrUpdate = """
        INSERT INTO tb_user (name,unique_id,last_ip,db_id,create_time)
        VALUES (?,?,?,?,?)
        ON CONFLICT (unique_id) DO UPDATE SET 
            last_ip = EXCLUDED.last_ip
    """

    fun init() {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.addBatch(createSequence)
                stmt.addBatch(createTable)
                stmt.executeBatch()
            }
        }
    }


    fun initUser(name: String, uniqueId: UUID, lastIp: String): User {
        dataSource.connection.use { conn ->
            conn.prepareStatement(insertOrUpdate).use { ps ->
                ps.setString(1, name)
                ps.setString(2, uniqueId.toString())
                ps.setString(3, lastIp)
                ps.setInt(4, 1) // todo dbId
                ps.setLong(5, System.currentTimeMillis())
                ps.executeUpdate()
            }
            conn.prepareStatement(select).use { ps ->
                ps.setString(1, uniqueId.toString())
                ps.executeQuery().use { rs ->
                    rs.next()
                    return User().also {
                        it.playerId = rs.getInt(1)
                        it.name = rs.getString(2)
                        it.uniqueId = UUID.fromString(rs.getString(3))
                        it.lastIp = rs.getString(4)
                        it.dbId = rs.getInt(5)
                        it.createTime = rs.getLong(6)
                    }
                }
            }

        }
    }

}
