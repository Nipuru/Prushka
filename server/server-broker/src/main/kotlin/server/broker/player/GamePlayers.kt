package server.broker.player

import server.common.logger.Logger
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object GamePlayers {
    private val byName = ConcurrentHashMap<String, GamePlayer>()

    fun registerPlayer(player: GamePlayer) {
        Logger.info("Register GamePlayer: {}", player.name)
        byName[player.name] = player
    }


    fun getPlayer(name: String) = byName[name]

    val players: Collection<GamePlayer>
        get() = Collections.unmodifiableCollection(byName.values)

    fun removePlayer(player: GamePlayer) {
        Logger.info("Remove GamePlayer: {}", player.name)
        byName.remove(player.name)
    }
}
