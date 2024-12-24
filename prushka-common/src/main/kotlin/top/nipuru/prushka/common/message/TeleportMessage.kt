package top.nipuru.prushka.common.message

import top.nipuru.prushka.common.message.shared.PlayerInfoMessage
import java.io.Serializable


/**
 * @author Nipuru
 * @since 2024/11/20 13:58
 */

enum class TeleportType {
    TPA, TPAHERE
}

class LocationMessage(
    val serverType: String,
    val worldName: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val pitch: Float,
    val yaw: Float,
) : Serializable {
    constructor(
        serverType: String, worldName: String, x: Double, y: Double, z: Double
    ) : this(
        serverType, worldName, x, y, z, 0.0F, 0.0F
    )

    fun clone(): LocationMessage {
        return LocationMessage(serverType, worldName, x, y, z, pitch, yaw)
    }
}

class TeleportRequestMessage(
    val sender: PlayerInfoMessage,
    val receiver: String,
    val type: TeleportType
) : Serializable {
    companion object {
        const val SUCCESS = 1                 // 成功 (等待回应)
        const val PLAYER_NOT_ONLINE = 2       // 玩家不在线
        const val REQUEST_ALREADY_EXISTS = 3  // 已经发送过请求了，稍后再试
        const val PLAYER_NOT_AVAILABLE = 4    // 玩家无法作出回应
        const val REQUEST_DISABLED = 5        // 玩家关闭了传送请求
        const val FRIEND_DIRECT = 6           // 好友快捷传送
    }
}

class TeleportResponseMessage(
    val sender: PlayerInfoMessage,
    val receiver: String,
    val type: TeleportType,
    val accepted: Boolean,
) : Serializable {
    companion object {
        const val SUCCESS = 1               // 成功
        const val PLAYER_NOT_ONLINE = 2     // 玩家不在线
        const val PLAYER_NOT_AVAILABLE = 3  // 玩家无法作出回应
    }
}

class GetPlayerLocationRequest(val name: String) : Serializable

class TeleportInvokeRequest(val from: String, val to: String) : Serializable

class TeleportOrSpawnRequest(val name: String, val location: LocationMessage) : Serializable


