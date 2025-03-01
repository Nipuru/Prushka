package top.nipuru.prushka.auth.service

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import top.nipuru.prushka.auth.schema.PlayerTable
import top.nipuru.prushka.common.message.auth.PlayerMessage
import java.util.*
import kotlin.math.abs


/**
 * @author Nipuru
 * @since 2024/11/07 17:28
 */
object PlayerService {

    var bdIdPool = listOf(1)

    init {
        transaction { SchemaUtils.create(PlayerTable) }
    }

    fun initPlayer(name: String, uniqueId: UUID, lastIp: String): PlayerMessage {
        return transaction {
            val rs = PlayerTable.selectAll().where(PlayerTable.uniqueId eq uniqueId.toString())
                .singleOrNull()
            if (rs != null) {
                return@transaction PlayerMessage(rs[PlayerTable.playerId], rs[PlayerTable.dbId])
            }

            val dbId = allocateDbId(uniqueId)
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

    // 分配DBID
    fun allocateDbId(uniqueId: UUID): Int {
        // 通过UUID计算
        val hash = uniqueId.hashCode()
        // 取余
        val index = abs(hash) % bdIdPool.size
        return bdIdPool[index]
    }
}
