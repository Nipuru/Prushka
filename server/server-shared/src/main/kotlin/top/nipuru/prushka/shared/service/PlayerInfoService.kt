package top.nipuru.prushka.shared.service

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import top.nipuru.prushka.common.message.shared.PlayerInfoMessage
import top.nipuru.prushka.shared.schema.PlayerInfoTable
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

object PlayerInfoService {
    private val byId = ConcurrentHashMap<Int, PlayerInfoMessage>()
    private val byName = ConcurrentHashMap<String, PlayerInfoMessage>()

    init {
        transaction {
            SchemaUtils.create(PlayerInfoTable)
            SchemaUtils.createMissingTablesAndColumns(PlayerInfoTable)
        }
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
            PlayerInfoTable.selectAll().where { PlayerInfoTable.playerId inList query }.forEach {
                val info = it.toPlayerInfoMessage()
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

        transaction {
            PlayerInfoTable.selectAll().where { PlayerInfoTable.name eq name }.forEach {
                val info = it.toPlayerInfoMessage()
                byId[info.playerId] = info
                byName[info.name] = info
            }
        }
        return byName[name]
    }

    fun insertOrUpdate(playerInfo: PlayerInfoMessage) {
        transaction {
            PlayerInfoTable.upsert(PlayerInfoTable.playerId) {
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
            byId[playerInfo.playerId] = playerInfo
            byName[playerInfo.name] = playerInfo
        }
    }

    private fun ResultRow.toPlayerInfoMessage(): PlayerInfoMessage {
        val info = PlayerInfoMessage()
        info.playerId = this[PlayerInfoTable.playerId]
        info.name = this[PlayerInfoTable.name]
        info.dbId = this[PlayerInfoTable.dbId]
        info.coin = this[PlayerInfoTable.coin]
        info.rankId = this[PlayerInfoTable.rankId]
        info.createTime = this[PlayerInfoTable.createTime]
        info.logoutTime = this[PlayerInfoTable.logoutTime]
        info.playedTime = this[PlayerInfoTable.playedTime]
        info.texture = this[PlayerInfoTable.texture].toTypedArray()
        return info
    }
}
