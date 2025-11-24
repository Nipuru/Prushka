package server.database.service

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import server.common.service.OfflineDataService
import server.database.schema.OfflineDataTable

object OfflineDataServiceImpl : OfflineDataService {

    override fun insert(playerId: Int, module: String, data: String, duplicateKey: String?) {
        transaction {
            if (!duplicateKey.isNullOrEmpty()) {
                val count = OfflineDataTable.selectAll().where {
                    (OfflineDataTable.playerId eq playerId) and
                            (OfflineDataTable.module eq module) and
                            (OfflineDataTable.duplicateKey eq duplicateKey)
                }.count()
                if (count > 0L) return@transaction
                OfflineDataTable.insert {
                    it[this.playerId] = playerId
                    it[this.module] = module
                    it[this.data] = data
                    it[this.duplicateKey] = duplicateKey
                }
            } else {
                OfflineDataTable.insert {
                    it[this.playerId] = playerId
                    it[this.module] = module
                    it[this.data] = data
                }
            }
        }
    }

}
