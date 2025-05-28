package top.nipuru.prushka.server.game.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import org.bukkit.Bukkit
import org.bukkit.Location
import top.nipuru.prushka.server.common.message.*
import top.nipuru.prushka.server.game.MessageType
import top.nipuru.prushka.server.game.gameplay.player.GamePlayers
import top.nipuru.prushka.server.game.util.submit


/**
 * @author Nipuru
 * @since 2024/11/21 15:31
 */
class GetPlayerLocationGameProcessor : AsyncUserProcessor<GetPlayerLocationRequest>() {
    override fun handleRequest(context: BizContext, asyncContext: AsyncContext, message: GetPlayerLocationRequest) {
        submit(async = false) {
            asyncContext.sendResponse(handle(message))
        }
    }

    private fun handle(message: GetPlayerLocationRequest): LocationMessage? {
        val bukkitPlayer = Bukkit.getPlayerExact(message.name) ?: return null
        val player = GamePlayers.getPlayer(bukkitPlayer.uniqueId)
        val location = player.teleport.lastLocation
        return LocationMessage(location.worldName, location.x, location.y, location.z)
    }

    override fun interest(): String {
        return GetPlayerLocationRequest::class.java.name
    }
}


class TeleportOrSpawnGameProcessor(private val spawnLocations: MutableMap<String, Location>) : AsyncUserProcessor<TeleportOrSpawnRequest>() {
    override fun handleRequest(context: BizContext, asyncContext: AsyncContext, message: TeleportOrSpawnRequest) {
        submit(async = false) {
            handle(message)
        }
    }

    private fun handle(message: TeleportOrSpawnRequest) {
        val world = Bukkit.getWorld(message.location.worldName) ?: return
        val location = Location(world, message.location.x, message.location.y, message.location.z)
        val bukkitPlayer = Bukkit.getPlayerExact(message.name)
        if (bukkitPlayer != null) {
            location.yaw = bukkitPlayer.location.yaw
            location.pitch = bukkitPlayer.location.pitch
            bukkitPlayer.teleportAsync(location)
            return
        }
        spawnLocations[message.name] = location
    }

    override fun interest(): String {
        return TeleportOrSpawnRequest::class.java.name
    }
}

