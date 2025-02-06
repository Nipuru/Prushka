package top.nipuru.prushka.broker.player

import net.afyer.afybroker.server.proxy.BrokerPlayer
import top.nipuru.prushka.broker.util.LeakBucketLimiter
import java.util.*

class GamePlayer(val brokerPlayer: BrokerPlayer) {
    // 每秒允许 0.25条消息，最多积累3条消息
    val chatLimiter: LeakBucketLimiter = LeakBucketLimiter(0.25, 3)

    val name: String
        get() = brokerPlayer.name

    val uniqueId: UUID
        get() = brokerPlayer.uniqueId
}
