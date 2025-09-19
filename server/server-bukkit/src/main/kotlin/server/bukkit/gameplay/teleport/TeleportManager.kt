package server.bukkit.gameplay.teleport

import net.afyer.afybroker.client.Broker
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import server.bukkit.gameplay.player.*
import server.common.message.TeleportInvokeRequest
import server.common.message.TeleportType
import java.util.concurrent.CompletableFuture


/**
 * @author Nipuru
 * @since 2024/11/20 16:03
 */
class TeleportManager(player: GamePlayer) : BaseManager(player) {

    lateinit var lastLocation: LocationData private set

    fun preload(request: TableInfos) {
        player.bukkitPlayer.location
        request.preload<LocationData>()
    }

    fun unpack(dataInfo: DataInfo) {
        lastLocation = dataInfo.unpack<LocationData>() ?: player.insert(LocationData())
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

    fun teleport(playerName: String, type: TeleportType): CompletableFuture<Boolean> {
        val message = when (type) {
            TeleportType.TPA -> TeleportInvokeRequest(froms = listOf(player.name), to = playerName)
            TeleportType.TPAHERE -> TeleportInvokeRequest(froms = listOf(playerName), to = player.name)
        }
        return CompletableFuture.supplyAsync {
            Broker.invokeSync(message)
        }
    }


    fun setLastLocation(location: Location) {
        if (!isSafeLocation(location)) return
        lastLocation = location.convert()
    }

    private fun Location.convert() = LocationData(
        worldName = world.name,
        x = x,
        y = y,
        z = z,
        pitch = pitch,
        yaw = yaw
    )

    private fun isSafeLocation(location: Location?): Boolean {
        if (location == null) return false
        val block = location.block
        val bottom = block.getRelative(BlockFace.DOWN)
        if (block.isSolid && block.type == Material.LAVA) return false
        if (!bottom.isSolid) return false
        return true
    }
}
