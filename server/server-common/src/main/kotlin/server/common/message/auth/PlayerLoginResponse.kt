package server.common.message.auth

import java.io.Serializable

class PlayerLoginResponse(val playerId: Int, val dbId: Int) : Serializable
