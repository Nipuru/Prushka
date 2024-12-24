package top.nipuru.prushka.game.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import org.bukkit.Bukkit
import org.bukkit.Location
import top.nipuru.prushka.common.message.*
import top.nipuru.prushka.game.MessageType
import top.nipuru.prushka.game.gameplay.player.GamePlayers
import top.nipuru.prushka.game.util.submit


/**
 * @author Nipuru
 * @since 2024/11/21 15:31
 */
class TeleportRequestGameProcessor : AsyncUserProcessor<TeleportRequestMessage>() {
    override fun handleRequest(context: BizContext, asyncContext: AsyncContext, message: TeleportRequestMessage) {
        submit(async = false) {
            asyncContext.sendResponse(handle(message))
        }
    }

    private fun handle(message: TeleportRequestMessage): Int {
        val bukkitPlayer = Bukkit.getPlayerExact(message.receiver) ?: return TeleportRequestMessage.PLAYER_NOT_ONLINE
        val player = GamePlayers.getPlayer(bukkitPlayer.uniqueId)
        player.teleport.addRequest(message.sender.name, message.type)
        return TeleportRequestMessage.SUCCESS
    }

    override fun interest(): String {
        return TeleportRequestMessage::class.java.name
    }
}

class TeleportResponseGameProcessor : AsyncUserProcessor<TeleportResponseMessage>() {
    override fun handleRequest(context: BizContext, asyncContext: AsyncContext, message: TeleportResponseMessage) {
        submit(async = false) {
            asyncContext.sendResponse(handle(message))
        }
    }

    private fun handle(message: TeleportResponseMessage): Int {
        val bukkitPlayer = Bukkit.getPlayerExact(message.receiver) ?: return TeleportResponseMessage.PLAYER_NOT_ONLINE

        if (!message.accepted) {
            MessageType.FAILED.sendMessage(bukkitPlayer, "玩家 ${message.sender.name} 拒绝了你的传送请求")
            return TeleportRequestMessage.SUCCESS
        }
        MessageType.INFO.sendMessage(bukkitPlayer, "玩家 ${message.sender.name} 接受了你的传送请求")
        if (message.type == TeleportType.TPA) {
            MessageType.INFO.sendMessage(bukkitPlayer, "传送将在 3 秒后开始.")
            val player = GamePlayers.getPlayer(bukkitPlayer.uniqueId)
            player.teleport.teleport(message.sender.name, TeleportType.TPA)
        }
        return TeleportRequestMessage.SUCCESS
    }

    override fun interest(): String {
        return TeleportResponseMessage::class.java.name
    }
}

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
        if (location.serverType.isEmpty()) {
            return null
        }
        return LocationMessage(location.serverType, location.worldName, location.x, location.y, location.z)
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

