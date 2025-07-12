package server.database.service

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import server.common.service.OfflineDataService
import server.database.schema.OfflineDataTable

class OfflineDataServiceImpl : OfflineDataService {

    override fun insert(playerId: Int, module: String, data: String) {
        transaction {
            OfflineDataTable.insert {
                it[this.playerId] = playerId
                it[this.module] = module
                it[this.data] = data
            }
        }
    }

}
