package server.broker.player

import server.common.logger.Logger
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ServerPlayerManager {
    private val byName = ConcurrentHashMap<String, ServerPlayer>()

    fun registerPlayer(player: ServerPlayer) {
        Logger.info("Register GamePlayer: {}", player.name)
        byName[player.name] = player
    }


    fun getPlayer(name: String) = byName[name]

    val players: Collection<ServerPlayer>
        get() = Collections.unmodifiableCollection(byName.values)

    fun removePlayer(player: ServerPlayer) {
        Logger.info("Remove GamePlayer: {}", player.name)
        byName.remove(player.name)
    }
}
