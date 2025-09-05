package server.common.service

import server.common.message.PlayerMessage
import java.util.*


/**
 * @author Nipuru
 * @since 2025/07/12 14:25
 */
interface PlayerService {

    fun load(name: String, uniqueId: UUID, lastIp: String): PlayerMessage


}