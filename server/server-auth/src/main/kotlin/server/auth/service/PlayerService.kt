package server.auth.service

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import server.auth.schema.PlayerTable
import server.common.message.auth.PlayerLoginResponse
import java.util.*


/**
 * @author Nipuru
 * @since 2024/11/07 17:28
 */
object PlayerService {


    fun initPlayer(name: String, uniqueId: UUID, lastIp: String): PlayerLoginResponse {
        return transaction {
            val rs = PlayerTable.selectAll().where(PlayerTable.uniqueId eq uniqueId.toString())
                .singleOrNull()
            if (rs != null) {
                return@transaction PlayerLoginResponse(
                    rs[PlayerTable.playerId],
                    rs[PlayerTable.dbId]
                )
            }

            val dbId = 1
            val playerId = PlayerTable.insert {
                it[this.name] = name
                it[this.uniqueId] = uniqueId.toString()
                it[this.lastIp] = lastIp
                it[this.dbId] = dbId
                it[this.createTime] = System.currentTimeMillis()
            } get PlayerTable.playerId
            return@transaction PlayerLoginResponse(playerId, dbId)
        }
    }
}
