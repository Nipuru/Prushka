package top.nipuru.minegame.common.message

import top.nipuru.minegame.common.message.database.FieldMessage
import java.io.Serializable
import java.util.*

class PlayerDataTransferRequest(val uniqueId: UUID) : Serializable

class PlayerDataMessage(val playerId: Int, val dbId: Int, val data: MutableMap<String, MutableList<List<FieldMessage>>>) : Serializable
