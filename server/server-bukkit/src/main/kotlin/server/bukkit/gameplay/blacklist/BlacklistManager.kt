package server.bukkit.gameplay.blacklist

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import server.bukkit.gameplay.player.*


/**
 * @author Nipuru
 * @since 2025/06/10 16:59
 */
class BlacklistManager(player: GamePlayer) : BaseManager(player) {

    private val moduleName = "blacklist"
    private val inboundBlacklists = Int2ObjectOpenHashMap<BlacklistInboundData>()
    private val outboundBlacklists = Int2ObjectOpenHashMap<BlacklistOutboundData>()

    fun preload(request: TableInfos) {
        request.preload<BlacklistInboundData>()
        request.preload<BlacklistInboundData>()
    }

    fun unpack(dataInfo: DataInfo) {
        dataInfo.unpackList<BlacklistInboundData>()
            .forEach{ inboundBlacklists.put(it.blockerId, it) }
        dataInfo.unpackList<BlacklistOutboundData>()
            .forEach{ outboundBlacklists.put(it.blockedId, it) }
    }

    fun pack(dataInfo: DataInfo) {
        inboundBlacklists.values.forEach(dataInfo::pack)
        outboundBlacklists.values.forEach(dataInfo::pack)
    }

    fun init() {
        player.offline.registerHandler(moduleName, this::handleOfflineData)
    }

    private fun handleOfflineData(data: String, isOnline: Boolean): Boolean {
        return true
    }
}