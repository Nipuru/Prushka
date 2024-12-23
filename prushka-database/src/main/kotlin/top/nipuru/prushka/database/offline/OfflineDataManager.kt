package top.nipuru.prushka.database.offline

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import top.nipuru.prushka.common.message.PlayerOfflineDataMessage

object OfflineDataManager {

    fun init() {
        transaction { SchemaUtils.create(OfflineDatas) }
    }

    fun insert(data: PlayerOfflineDataMessage) {
        transaction {
            OfflineDatas.insert {
                it[playerId] = data.playerId
                it[module] = data.module
                it[this.data] = data.data
            }
        }
    }

}
