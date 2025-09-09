package server.common.message

import java.io.Serializable


/**
 * 在线玩家列表同步消息
 *
 * @author Nipuru
 * @since 2025/09/08 20:17
 */
data class OnlinePlayersMessage(val onlineList: List<String> = emptyList(), val offlineList: List<String> = emptyList()) : Serializable
