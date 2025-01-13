package top.nipuru.prushka.shared.player

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import top.nipuru.prushka.common.message.shared.PlayerInfoMessage
import java.util.concurrent.ConcurrentHashMap

object PlayerInfoManager {
    private val byId = ConcurrentHashMap<Int, PlayerInfoMessage>()
    private val byName = ConcurrentHashMap<String, PlayerInfoMessage>()

    fun init() {
        transaction { SchemaUtils.create(PlayerInfos) }
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
            PlayerInfos.select(PlayerInfos.playerId inList query).forEach {
                val info = PlayerInfoMessage()
                info.playerId = it[PlayerInfos.playerId]
                info.name = it[PlayerInfos.name]
                info.dbId = it[PlayerInfos.dbId]
                info.coin = it[PlayerInfos.coin]
                info.rankId = it[PlayerInfos.rankId]
                info.createTime = it[PlayerInfos.createTime]
                info.logoutTime = it[PlayerInfos.logoutTime]
                info.playedTime = it[PlayerInfos.playedTime]
                info.texture = it[PlayerInfos.texture].toTypedArray()

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
            PlayerInfos.select(PlayerInfos.name eq name).forEach {
                val info = PlayerInfoMessage()
                info.playerId = it[PlayerInfos.playerId]
                info.name = it[PlayerInfos.name]
                info.dbId = it[PlayerInfos.dbId]
                info.coin = it[PlayerInfos.coin]
                info.rankId = it[PlayerInfos.rankId]
                info.createTime = it[PlayerInfos.createTime]
                info.logoutTime = it[PlayerInfos.logoutTime]
                info.playedTime = it[PlayerInfos.playedTime]
                info.texture = it[PlayerInfos.texture].toTypedArray()

                byId[info.playerId] = info
                byName[info.name] = info
                return@transaction info
            }
            return@transaction null
        }
    }

    fun insertOrUpdate(playerInfo: PlayerInfoMessage) {
        transaction {
            val result = PlayerInfos.update({ PlayerInfos.playerId eq playerInfo.playerId }) {
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
                PlayerInfos.insert {
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
