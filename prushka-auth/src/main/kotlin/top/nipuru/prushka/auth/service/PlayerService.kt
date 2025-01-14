package top.nipuru.prushka.auth.service

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import top.nipuru.prushka.auth.schema.PlayerTable
import top.nipuru.prushka.common.message.auth.PlayerMessage
import java.util.*


/**
 * @author Nipuru
 * @since 2024/11/07 17:28
 */
object PlayerService {

    fun init() {
        transaction { SchemaUtils.create(PlayerTable) }
    }

    fun initPlayer(name: String, uniqueId: UUID, lastIp: String): PlayerMessage {
        return transaction {
            val rs = PlayerTable.select(PlayerTable.uniqueId eq uniqueId.toString())
                .singleOrNull()
            if (rs != null) {
                return@transaction PlayerMessage(rs[PlayerTable.playerId], rs[PlayerTable.dbId])
            }

            val dbId = 1
            val playerId = PlayerTable.insert {
                it[this.name] = name
                it[this.uniqueId] = uniqueId.toString()
                it[this.lastIp] = lastIp
                it[this.dbId] = dbId
                it[this.createTime] = System.currentTimeMillis()
            } get PlayerTable.playerId
            return@transaction PlayerMessage(playerId, dbId)
        }
    }
}
