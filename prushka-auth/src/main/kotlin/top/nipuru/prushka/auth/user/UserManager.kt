package top.nipuru.prushka.auth.user

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import top.nipuru.prushka.common.message.auth.UserMessage
import java.util.*


/**
 * @author Nipuru
 * @since 2024/11/07 17:28
 */
object UserManager {

    fun init() {
        transaction { SchemaUtils.create(Users) }
    }

    fun initUser(name: String, uniqueId: UUID, lastIp: String): UserMessage {
        return transaction {
            val rs = Users.select(Users.uniqueId eq uniqueId.toString())
                .singleOrNull()
            if (rs != null) {
                return@transaction UserMessage(rs[Users.playerId], rs[Users.dbId])
            }

            val dbId = 1
            val playerId = Users.insert {
                it[this.name] = name
                it[this.uniqueId] = uniqueId.toString()
                it[this.lastIp] = lastIp
                it[this.dbId] = dbId
                it[this.createTime] = System.currentTimeMillis()
            } get Users.playerId
            return@transaction UserMessage(playerId, dbId)
        }
    }
}
