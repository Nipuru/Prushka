package server.common.message.database

import java.io.Serializable

class PlayerDataMessage(val playerId: Int, val dbId: Int, val data: MutableMap<String, MutableList<List<FieldMessage>>>) : Serializable
