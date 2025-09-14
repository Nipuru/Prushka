package server.common.message

import java.io.Serializable
import java.util.*


/**
 * @author Nipuru
 * @since 2025/09/14 17:05
 */
class SystemChatMessage(val messages: List<Message>): Serializable {
    class Message(val receiver: UUID, val message: String): Serializable
}