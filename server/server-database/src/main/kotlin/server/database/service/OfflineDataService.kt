package server.database.service

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import server.common.message.PlayerOfflineDataMessage
import server.database.schema.OfflineDataTable

object OfflineDataService {

    init {
        transaction { SchemaUtils.create(OfflineDataTable) }
    }

    fun insert(offline: PlayerOfflineDataMessage) {
        transaction {
            OfflineDataTable.insert {
                it[playerId] = offline.playerId
                it[module] = offline.module
                it[this.data] = offline.data
            }
        }
    }

}
