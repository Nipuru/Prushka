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
        class ActionBar(receiver: UUID, val message: String): Message(receiver)
        class PlayerListHeader(receiver: UUID, val header: String): Message(receiver)
        class PlayerListFooter(receiver: UUID, val footer: String): Message(receiver)
        class PlayerListHeaderAndFooter(receiver: UUID, val header: String, val footer: String): Message(receiver)
        class TitlePartTitle(receiver: UUID, val title: String): Message(receiver)
        class TitlePartSubtitle(receiver: UUID, val subtitle: String): Message(receiver)
        class TitlePartTimes(receiver: UUID, val fadeIn: Long, val stay: Long, val fadeOut: Long): Message(receiver)
        class TitleClear(receiver: UUID): Message(receiver)
        class TitleReset(receiver: UUID): Message(receiver)
        class Book(receiver: UUID, val title: String, val author: String, val pages: List<String>): Message(receiver)
    }
}