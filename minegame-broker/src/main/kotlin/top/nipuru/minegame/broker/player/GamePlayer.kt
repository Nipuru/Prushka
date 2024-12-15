package top.nipuru.minegame.broker.player

import net.afyer.afybroker.server.proxy.BrokerPlayer
import top.nipuru.minegame.broker.util.LeakBucketLimiter
import java.util.*

class GamePlayer(val brokerPlayer: BrokerPlayer) {

    val name: String
        get() = brokerPlayer.name

    val uniqueId: UUID
        get() = brokerPlayer.uniqueId
}
