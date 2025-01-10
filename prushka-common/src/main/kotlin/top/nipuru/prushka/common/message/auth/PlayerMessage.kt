package top.nipuru.prushka.common.message.auth

import java.io.Serializable
import java.util.*

class PlayerRequestMessage(val name: String, val uniqueId: UUID, val ip: String) : Serializable

class PlayerMessage(val playerId: Int, val dbId: Int) : Serializable
