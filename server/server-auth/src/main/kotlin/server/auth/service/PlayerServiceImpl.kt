package server.auth.service

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import server.auth.schema.PlayerTable
import server.common.message.PlayerMessage
import server.common.service.PlayerService
import java.util.*


/**
 * @author Nipuru
 * @since 2024/11/07 17:28
 */
class PlayerServiceImpl: PlayerService {
    override fun load(name: String, uniqueId: UUID, lastIp: String): PlayerMessage {
        return transaction {
            val rs = PlayerTable.selectAll().where(PlayerTable.uniqueId eq uniqueId)
                .singleOrNull()
            if (rs != null) {
                PlayerTable.update({ PlayerTable.uniqueId eq uniqueId }) {
                    it[PlayerTable.lastIp] = lastIp
                }
                PlayerMessage(rs[PlayerTable.playerId], rs[PlayerTable.dbId])
            } else {
                val dbId = selectDbId()
                val playerId = PlayerTable.insert {
                    it[this.name] = name
                    it[this.uniqueId] = uniqueId
                    it[this.lastIp] = lastIp
                    it[this.dbId] = dbId
                    it[this.createTime] = System.currentTimeMillis()
                } get PlayerTable.playerId
                PlayerMessage(playerId, dbId)
            }
        }
    }

    private fun selectDbId(): Int {
        return 1 // todo
    }
}
