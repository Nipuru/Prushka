package top.nipuru.prushka.shared.service

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import top.nipuru.prushka.common.message.shared.PlayerInfoMessage
import top.nipuru.prushka.shared.schema.PlayerInfoTable
import java.util.concurrent.ConcurrentHashMap

object PlayerInfoService {
    private val byId = ConcurrentHashMap<Int, PlayerInfoMessage>()
    private val byName = ConcurrentHashMap<String, PlayerInfoMessage>()

    fun init() {
        transaction { SchemaUtils.create(PlayerInfoTable) }
    }

    fun getByIds(playerIds: List<Int>): Map<Int, PlayerInfoMessage> {
        val result = mutableMapOf<Int, PlayerInfoMessage>()
        val query = mutableListOf<Int>()
        for (playerId in playerIds) {
            if (byId.contains(playerId)) {
                result[playerId] = byId[playerId]!!
            } else {
                query.add(playerId)
            }
        }
        if (query.isEmpty()) {
            return result
        }
        transaction {
            PlayerInfoTable.select(PlayerInfoTable.playerId inList query).forEach {
                val info = PlayerInfoMessage()
                info.playerId = it[PlayerInfoTable.playerId]
                info.name = it[PlayerInfoTable.name]
                info.dbId = it[PlayerInfoTable.dbId]
                info.coin = it[PlayerInfoTable.coin]
                info.rankId = it[PlayerInfoTable.rankId]
                info.createTime = it[PlayerInfoTable.createTime]
                info.logoutTime = it[PlayerInfoTable.logoutTime]
                info.playedTime = it[PlayerInfoTable.playedTime]
                info.texture = it[PlayerInfoTable.texture].toTypedArray()

                byId[info.playerId] = info
                byName[info.name] = info
                result[info.playerId] = info
            }
        }
        return result
    }

    fun getByName(name: String): PlayerInfoMessage? {
        if (byName.contains(name)) {
            return byName[name]
        }

        return transaction {
            PlayerInfoTable.select(PlayerInfoTable.name eq name).forEach {
                val info = PlayerInfoMessage()
                info.playerId = it[PlayerInfoTable.playerId]
                info.name = it[PlayerInfoTable.name]
                info.dbId = it[PlayerInfoTable.dbId]
                info.coin = it[PlayerInfoTable.coin]
                info.rankId = it[PlayerInfoTable.rankId]
                info.createTime = it[PlayerInfoTable.createTime]
                info.logoutTime = it[PlayerInfoTable.logoutTime]
                info.playedTime = it[PlayerInfoTable.playedTime]
                info.texture = it[PlayerInfoTable.texture].toTypedArray()

                byId[info.playerId] = info
                byName[info.name] = info
                return@transaction info
            }
            return@transaction null
        }
    }

    fun insertOrUpdate(playerInfo: PlayerInfoMessage) {
        transaction {
            val result = PlayerInfoTable.update({ PlayerInfoTable.playerId eq playerInfo.playerId }) {
                it[name] = playerInfo.name
                it[dbId] = playerInfo.dbId
                it[coin] = playerInfo.coin
                it[rankId] = playerInfo.rankId
                it[createTime] = playerInfo.createTime
                it[logoutTime] = playerInfo.logoutTime
                it[playedTime] = playerInfo.playedTime
                it[texture] = playerInfo.texture.toList()
            }
            if (result == 0) {
                PlayerInfoTable.insert {
                    it[playerId] = playerInfo.playerId
                    it[name] = playerInfo.name
                    it[dbId] = playerInfo.dbId
                    it[coin] = playerInfo.coin
                    it[rankId] = playerInfo.rankId
                    it[createTime] = playerInfo.createTime
                    it[logoutTime] = playerInfo.logoutTime
                    it[playedTime] = playerInfo.playedTime
                    it[texture] = playerInfo.texture.toList()
                }
            }
            byId[playerInfo.playerId] = playerInfo
            byName[playerInfo.name] = playerInfo
        }
    }
}
