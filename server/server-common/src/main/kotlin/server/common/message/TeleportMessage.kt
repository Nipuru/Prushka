package server.common.message

import java.io.Serializable


/**
 * @author Nipuru
 * @since 2024/11/20 13:58
 */

enum class TeleportType {
    TPA, TPAHERE
}

class LocationMessage(val worldName: String, val x: Double, val y: Double, val z: Double, val pitch: Float, val yaw: Float) : Serializable

class TeleportRequestMessage(
    val sender: PlayerInfoMessage,
    val receiver: String,
    val type: TeleportType
) : Serializable {

}

class TeleportResponseMessage(
    val sender: PlayerInfoMessage,
    val receiver: String,
    val type: TeleportType,
    val accepted: Boolean,
) : Serializable {

}

class GetPlayerLocationRequest(val name: String) : Serializable

class TeleportInvokeRequest(val froms: List<String>, val to: String) : Serializable

class TeleportOrSpawnRequest(val names: List<String>, val location: LocationMessage) : Serializable


