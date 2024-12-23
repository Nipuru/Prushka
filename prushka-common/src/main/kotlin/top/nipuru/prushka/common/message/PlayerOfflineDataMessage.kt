package top.nipuru.prushka.common.message

import java.io.Serializable

class PlayerOfflineDataMessage(val name: String, val playerId: Int, val dbId: Int, val module: String, val data: String) : Serializable
