package server.common.message

import java.io.Serializable
import java.util.*


/**
 * @author Nipuru
 * @since 2025/09/14 17:05
 */
class AudienceMessage(val messages: List<Message>): Serializable {
    sealed class Message(val receiver: UUID): Serializable {
        class SystemChat(receiver: UUID, val message: String): Message(receiver)
    }
}