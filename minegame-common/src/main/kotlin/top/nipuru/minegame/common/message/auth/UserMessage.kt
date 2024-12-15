package top.nipuru.minegame.common.message.auth

import java.io.Serializable
import java.util.*

class QueryUserRequest(val name: String, val uniqueId: UUID, val ip: String) : Serializable

class UserMessage(val playerId: Int, val dbId: Int) : Serializable
