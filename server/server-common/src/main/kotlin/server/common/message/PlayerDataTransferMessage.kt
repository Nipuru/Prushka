package server.common.message

import server.common.message.database.FieldMessage
import java.io.Serializable
import java.util.*

class PlayerDataTransferMessage(val uniqueId: UUID) : Serializable

class PlayerDataMessage(val playerId: Int, val dbId: Int, val data: MutableMap<String, MutableList<List<FieldMessage>>>) : Serializable

