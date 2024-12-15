package top.nipuru.minegame.auth.user

import java.util.*


/**
 * @author Nipuru
 * @since 2024/11/07 17:38
 */
class User {
    var playerId: Int = 0
    var name: String = ""
    var uniqueId: UUID = UUID(0L, 0L)
    var lastIp: String = ""
    var dbId: Int = 0
    var createTime: Long = 0L
}
