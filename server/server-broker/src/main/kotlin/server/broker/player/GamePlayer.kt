package server.broker.player

import net.afyer.afybroker.server.proxy.BrokerPlayer
import java.util.*

class GamePlayer(val brokerPlayer: BrokerPlayer) {

    val name: String
        get() = brokerPlayer.name

    val uniqueId: UUID
        get() = brokerPlayer.uniqueId
}
