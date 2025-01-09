package top.nipuru.prushka.database.offline

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import top.nipuru.prushka.common.message.PlayerOfflineDataMessage

object OfflineDataManager {

    fun init() {
        transaction { SchemaUtils.create(OfflineDatas) }
    }

    fun insert(offline: PlayerOfflineDataMessage) {
        transaction {
            OfflineDatas.insert {
                it[playerId] = offline.playerId
                it[module] = offline.module
                it[this.data] = offline.data
            }
        }
    }

}
