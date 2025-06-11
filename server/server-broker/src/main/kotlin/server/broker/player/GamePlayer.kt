package server.broker.player

import net.afyer.afybroker.server.proxy.BrokerPlayer
import server.broker.util.LeakBucketLimiter
import java.util.*
import java.util.concurrent.locks.ReentrantLock

class GamePlayer(val brokerPlayer: BrokerPlayer) {
    // 每秒允许 0.25条消息，最多积累3条消息
    val chatLimiter = LeakBucketLimiter(0.25, 3)

    val name: String
        get() = brokerPlayer.name

    val uniqueId: UUID
        get() = brokerPlayer.uniqueId
}
