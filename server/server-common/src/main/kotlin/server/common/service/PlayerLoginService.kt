package server.common.service

import java.io.Serializable
import java.util.*


/**
 * @author Nipuru
 * @since 2025/07/12 14:25
 */
interface PlayerLoginService {

    fun login(name: String, uniqueId: UUID, lastIp: String): LoginData

    class LoginData(val playerId: Int, val dbId: Int) : Serializable
}