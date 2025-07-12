package server.common.message

import java.io.Serializable

class FieldMessage(val name: String, val value: Any) : Serializable
class PlayerDataMessage(val playerId: Int, val dbId: Int, val data: MutableMap<String, MutableList<List<FieldMessage>>>) : Serializable
