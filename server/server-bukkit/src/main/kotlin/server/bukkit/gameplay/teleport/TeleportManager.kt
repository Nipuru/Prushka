package server.bukkit.gameplay.teleport

import net.afyer.afybroker.client.Broker
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import server.bukkit.gameplay.player.*
import server.bukkit.util.submit
import server.common.message.TeleportInvokeRequest
import server.common.message.TeleportType


/**
 * @author Nipuru
 * @since 2024/11/20 16:03
 */
class TeleportManager(player: GamePlayer) : BaseManager(player) {

    lateinit var lastLocation: LocationData
        private set

    fun preload(request: TableInfos) {
        request.preload<LocationData>()
    }

    fun unpack(dataInfo: DataInfo) {
        lastLocation = dataInfo.unpack<LocationData>() ?: LocationData().also { player.insert(it) }
    }

    fun pack(dataInfo: DataInfo) {
        dataInfo.pack(lastLocation)
    }

    fun onJoin() {
        setLastLocation(player.bukkitPlayer.location)
    }

    fun onQuit() {
        player.update(lastLocation) // 退出的时候保存一下
    }

    fun teleport(playerName: String, type: TeleportType) {
        val message = when (type) {
            TeleportType.TPA -> TeleportInvokeRequest(player.name, playerName)
            TeleportType.TPAHERE -> TeleportInvokeRequest(playerName, player.name)
        }
        submit {
            Broker.oneway(message)
        }
    }


    fun setLastLocation(location: Location) {
        if (!isSafeLocation(location)) return
        lastLocation.also {
            it.worldName = location.world.name
            it.x = location.x
            it.y = location.y
            it.z = location.z
            it.pitch = location.pitch
            it.yaw = location.yaw
        }
    }

    private fun isSafeLocation(location: Location?): Boolean {
        if (location == null) return false
        val block = location.block
        val bottom = block.getRelative(BlockFace.DOWN)
        if (block.isSolid && block.type == Material.LAVA) return false
        if (!bottom.isSolid) return false
        return true
    }
}
