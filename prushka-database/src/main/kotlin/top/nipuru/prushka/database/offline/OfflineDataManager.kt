package top.nipuru.prushka.database.offline

import top.nipuru.prushka.common.message.PlayerOfflineDataMessage
import top.nipuru.prushka.database.dataSource
import top.nipuru.prushka.database.logger.logger
import top.nipuru.prushka.database.util.escape
import top.nipuru.prushka.database.util.executeSql

object OfflineDataManager {

    private val createTable = """
            CREATE TABLE IF NOT EXISTS tb_offline (
                id          BIGSERIAL    NOT NULL,
                player_id   INTEGER      NOT NULL,
                module      TEXT         NOT NULL,
                data        TEXT         NOT NULL,
                CONSTRAINT  pkey_tb_offline PRIMARY KEY (id)
            ); 
            """.trimIndent()

    private const val createIndex = "CREATE INDEX IF NOT EXISTS idx_player_id ON tb_offline (player_id);"

    private const val insert = "INSERT INTO tb_offline (player_id,module,data) VALUES (%d,'%s','%s');"

    fun init() {
        dataSource.connection.use { con ->
            con.createStatement().use { s ->
                s.addBatch(createTable)
                s.addBatch(createIndex)
                s.executeBatch()
            }
        }
    }

    fun insert(data: PlayerOfflineDataMessage) {
        dataSource.connection.use { con ->
            con.createStatement().use { s ->
                val sql = insert.format(data.playerId, data.module.escape(), data.data.escape())
                s.executeSql(sql)
            }
        }
    }

}
