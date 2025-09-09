package server.bukkit.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import org.bukkit.Bukkit
import org.bukkit.Location
import server.bukkit.BukkitPlugin
import server.bukkit.gameplay.player.gamePlayer
import server.bukkit.util.schedule
import server.common.message.GetPlayerLocationRequest
import server.common.message.LocationMessage
import server.common.message.TeleportOrSpawnRequest


/**
 * @author Nipuru
 * @since 2024/11/21 15:31
 */
class GetPlayerLocationBukkitProcessor : AsyncUserProcessor<GetPlayerLocationRequest>() {
    override fun handleRequest(context: BizContext, asyncContext: AsyncContext, message: GetPlayerLocationRequest) {
        BukkitPlugin.schedule {
            asyncContext.sendResponse(handle(message))
        }
    }

    private fun handle(message: GetPlayerLocationRequest): LocationMessage? {
        val bukkitPlayer = Bukkit.getPlayerExact(message.name) ?: return null
        val player = bukkitPlayer.gamePlayer
        val location = player.teleport.lastLocation
        return LocationMessage(location.worldName, location.x, location.y, location.z)
    }

    override fun interest(): String {
        return GetPlayerLocationRequest::class.java.name
    }
}


class TeleportOrSpawnBukkitProcessor(private val spawnLocations: MutableMap<String, Location>) : AsyncUserProcessor<TeleportOrSpawnRequest>() {
    override fun handleRequest(context: BizContext, asyncContext: AsyncContext, message: TeleportOrSpawnRequest) {
        BukkitPlugin.schedule {
            handle(message)
        }
    }

    private fun handle(message: TeleportOrSpawnRequest) {
        val world = Bukkit.getWorld(message.location.worldName) ?: return
        val location = Location(world, message.location.x, message.location.y, message.location.z)
        for (name in message.names) {
            val bukkitPlayer = Bukkit.getPlayerExact(name)
            if (bukkitPlayer != null) {
                location.yaw = bukkitPlayer.location.yaw
                location.pitch = bukkitPlayer.location.pitch
                bukkitPlayer.teleportAsync(location)
            } else {
                spawnLocations[name] = location
            }
        }
    }

    override fun interest(): String {
        return TeleportOrSpawnRequest::class.java.name
    }
}

