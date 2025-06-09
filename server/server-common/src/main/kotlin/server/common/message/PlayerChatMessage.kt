package server.common.message

import server.common.message.shared.PlayerInfoMessage
import java.io.Serializable

/** 公共聊天消息  */
class PlayerChatMessage(val sender: PlayerInfoMessage, val fragments: Array<FragmentMessage>) : Serializable {
    companion object {
        const val SUCCESS: Int = 0
        const val FAILURE: Int = 1
        const val RATE_LIMIT: Int = 3
    }
}

class PlayerPrivateChatMessage(
    val sender: PlayerInfoMessage,
    val fragments: Array<FragmentMessage>,
    val receiver: String
) : Serializable {
    companion object {
        const val SUCCESS: Int = 0
        const val FAILURE: Int = 1
        const val RATE_LIMIT: Int = 2
        const val NOT_ONLINE: Int = 3
        const val DENY: Int = 4
    }
}
