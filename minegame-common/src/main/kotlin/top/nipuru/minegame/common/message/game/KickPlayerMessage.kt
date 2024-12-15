package top.nipuru.minegame.common.message.game

import java.io.Serializable
import java.util.UUID


/**
 * @author Nipuru
 * @since 2024/12/03 16:38
 */
enum class KickReason {
    SYSTEM_ERROR,   // 系统发生错误
    FORBIDDEN,      // 账户被封停，联系管理人员
    REPAIR          // 服务器维护中，请稍后再进入游戏
}

class KickPlayerMessage(val uniqueId: UUID, val reason: KickReason) : Serializable
