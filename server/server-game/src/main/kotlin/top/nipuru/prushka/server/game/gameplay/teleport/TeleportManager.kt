package top.nipuru.prushka.server.game.gameplay.teleport

import net.afyer.afybroker.client.Broker
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import top.nipuru.prushka.server.common.message.TeleportInvokeRequest
import top.nipuru.prushka.server.common.message.TeleportRequestMessage
import top.nipuru.prushka.server.common.message.TeleportResponseMessage
import top.nipuru.prushka.server.common.message.TeleportType
import top.nipuru.prushka.server.common.message.database.PlayerDataRequestMessage
import top.nipuru.prushka.server.game.MessageType
import top.nipuru.prushka.server.game.gameplay.player.BaseManager
import top.nipuru.prushka.server.game.gameplay.player.DataInfo
import top.nipuru.prushka.server.game.gameplay.player.GamePlayer
import top.nipuru.prushka.server.game.gameplay.player.preload
import top.nipuru.prushka.server.game.plugin
import top.nipuru.prushka.server.game.util.submit
import java.util.concurrent.TimeUnit


/**
 * @author Nipuru
 * @since 2024/11/20 16:03
 */
class TeleportManager(player: GamePlayer) : BaseManager(player) {

    lateinit var lastLocation: LocationData
        private set

    fun preload(request: PlayerDataRequestMessage) {
        request.preload(LocationData::class.java)
    }

    fun unpack(dataInfo: DataInfo) {
        lastLocation = dataInfo.unpack(LocationData::class.java) ?: LocationData().also { player.insert(it) }
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
