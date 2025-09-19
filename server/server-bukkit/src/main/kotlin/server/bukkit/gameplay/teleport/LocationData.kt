package server.bukkit.gameplay.teleport

import server.bukkit.gameplay.player.Data
import server.bukkit.gameplay.player.Table


/**
 * @author Nipuru
 * @since 2024/11/21 13:41
 */
@Table(name = "tb_location")
class LocationData(
    /** 世界名字 */
    var worldName: String = "",

    /** x坐标 */
    var x: Double = 0.0,

    /** y坐标 */
    var y: Double = 0.0,

    /** z坐标 */
    var z: Double = 0.0,

    /** yaw */
    var yaw: Float = 0.0F,

    /** pitch */
    var pitch: Float = 0.0F,
) : Data
