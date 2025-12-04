package server.bukkit.gameplay.blacklist

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import server.bukkit.constant.OFFLINE_BLACKLIST_ADD
import server.bukkit.constant.OFFLINE_BLACKLIST_REMOVE
import server.bukkit.gameplay.player.*


/**
 * 黑名单模块
 *
 * @author Nipuru
 * @since 2025/06/10 16:59
 */
class BlacklistManager(player: GamePlayer) : BaseManager(player) {

    private val blacklists = Int2ObjectOpenHashMap<BlacklistData>()

    fun preload(request: TableInfos) {
        request.preload<BlacklistData>()
        request.preload<BlacklistData>()
    }

    fun unpack(dataInfo: DataInfo) {
        dataInfo.unpackList<BlacklistData>()
            .forEach{ blacklists.put(it.target, it) }
    }

    fun pack(dataInfo: DataInfo) {
        blacklists.values.forEach(dataInfo::pack)
    }

    /**
     * 判断是否屏蔽某个玩家
     */
    fun isBlocking(playerId: Int): Boolean {
        val blacklist = blacklists[playerId]
        return blacklist != null && blacklist.blocking
    }

    /**
     * 判断是否被某个玩家屏蔽
     */
    fun isBlockedBy(playerId: Int): Boolean {
        val blacklist = blacklists[playerId]
        return blacklist != null && blacklist.blocked
    }

    /**
     * 屏蔽某个玩家
     * [name] 玩家名称
     * [playerId] 玩家 id
     * [dbId] 数据库 id
     */
    fun add(name: String, playerId: Int, dbId: Int) {
        var blacklist = getOrCreate(playerId)
        blacklist.blocking = true
        player.update(blacklist, BlacklistData::blocking)
        player.offline.pushOfflineData(name, playerId, dbId, OFFLINE_BLACKLIST_ADD, playerId.toString(), playerId.toString())
    }

    /**
     * 取消屏蔽某个玩家
     * [name] 玩家名称
     * [playerId] 玩家 id
     * [dbId] 数据库 id
     */
    fun remove(name: String, playerId: Int, dbId: Int) {
        var blacklist = blacklists[playerId]
        if (blacklist == null) return
        blacklist.blocking = false
        player.update(blacklist, BlacklistData::blocking)
        cleanup(playerId)
        player.offline.pushOfflineData(name, playerId, dbId, OFFLINE_BLACKLIST_REMOVE, playerId.toString(), playerId.toString())
    }

    fun init() {
        player.offline.registerHandler(OFFLINE_BLACKLIST_ADD, this::handleBlacklistAdd)
        player.offline.registerHandler(OFFLINE_BLACKLIST_REMOVE, this::handleBlacklistRemove)
    }

    private fun handleBlacklistAdd(data: String, isOnline: Boolean) {
        val playerId = data.toInt()
        var blacklist = getOrCreate(playerId)
        blacklist.blocked = true
        player.update(blacklist, BlacklistData::blocked)
    }

    private fun handleBlacklistRemove(data: String, isOnline: Boolean) {
        val playerId = data.toInt()
        var blacklist = blacklists[playerId]
        if (blacklist == null) return
        blacklist.blocked = false
        player.update(blacklist, BlacklistData::blocked)
        cleanup(playerId)
    }

    private fun getOrCreate(playerId: Int): BlacklistData {
        var blacklist = blacklists[playerId]
        if (blacklist == null) {
            blacklist = BlacklistData()
            blacklist.target = playerId
            blacklists.put(playerId, blacklist)
            player.insert(blacklist)
        }
        return blacklist
    }

    private fun cleanup(playerId: Int) {
        // 删除无用数据
        val blacklist = blacklists[playerId]
        if (blacklist == null) return
        if (blacklist.blocking || blacklist.blocked) return
        blacklists.remove(playerId)
        player.delete(blacklist)
    }
}