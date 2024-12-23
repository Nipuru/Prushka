package top.nipuru.prushka.common.message.shared

import java.io.Serializable
import java.util.UUID

class PlayerInfoMessage : Serializable {
    /** 玩家id  */
    var playerId: Int = 0

    /** 玩家名字  */
    var name: String = ""

    /** dbId  */
    var dbId: Int = 0

    /** 货币  */
    var coin: Long = 0

    /** 头衔id  */
    var rankId: Int = 0

    /** 创建时间  */
    var createTime: Long = 0

    /** 最后离线时间  */
    var logoutTime: Long = 0

    /** 累计在线时间  */
    var playedTime: Long = 0

    /** 皮肤材质 长度2(value|signature) */
    var texture: Array<String> = emptyArray()
}

class PlayerInfoUpdateNotify(val playerInfo: PlayerInfoMessage) : Serializable
class GetPlayerInfoRequest(val name: String) : Serializable
class GetPlayerInfosRequest(val playerIds: List<Int>) : Serializable
