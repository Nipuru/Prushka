package server.common.message

import java.io.Serializable

/** 公共聊天消息  */
class PlayerChatMessage(val sender: PlayerInfoMessage, val fragments: Array<FragmentMessage>) : Serializable

class PlayerPrivateChatMessage(val sender: PlayerInfoMessage, val fragments: Array<FragmentMessage>, val receiver: String) : Serializable
