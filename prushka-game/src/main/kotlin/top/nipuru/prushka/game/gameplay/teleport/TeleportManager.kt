package top.nipuru.prushka.game.gameplay.teleport

import net.afyer.afybroker.client.Broker
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import top.nipuru.prushka.common.message.TeleportInvokeRequest
import top.nipuru.prushka.common.message.TeleportRequestMessage
import top.nipuru.prushka.common.message.TeleportResponseMessage
import top.nipuru.prushka.common.message.TeleportType
import top.nipuru.prushka.common.message.database.QueryPlayerRequest
import top.nipuru.prushka.game.MessageType
import top.nipuru.prushka.game.gameplay.player.BaseManager
import top.nipuru.prushka.game.gameplay.player.DataInfo
import top.nipuru.prushka.game.gameplay.player.GamePlayer
import top.nipuru.prushka.game.gameplay.player.preload
import top.nipuru.prushka.game.plugin
import top.nipuru.prushka.game.util.submit
import java.util.concurrent.TimeUnit


/**
 * @author Nipuru
 * @since 2024/11/20 16:03
 */
class TeleportManager(player: GamePlayer) : BaseManager(player) {

    private val expire = TimeUnit.SECONDS.toMillis(30)
    private val requests = mutableMapOf<String, TeleportRequestCache>()
        get() {
            val now = System.currentTimeMillis()
            field.values.removeIf { it.expireAt <= now }
            return field
        }
    private val histories = mutableMapOf<String, TeleportHistory>()
    lateinit var lastLocation: LocationData
        private set

    fun preload(request: QueryPlayerRequest) {
        request.preload(TeleportHistory::class.java)
        request.preload(LocationData::class.java)
    }

    fun unpack(dataInfo: DataInfo) {
        dataInfo.unpackList(TeleportRequestCache::class.java)
            .forEach { requests[it.sender] = it }
        dataInfo.unpackList(TeleportHistory::class.java)
            .forEach { histories[it.player] = it }
        lastLocation = dataInfo.unpack(LocationData::class.java) ?: LocationData().also { player.insert(it) }
    }

    fun pack(dataInfo: DataInfo) {
        requests.values.forEach(dataInfo::pack)
        histories.values.forEach(dataInfo::pack)
        dataInfo.pack(lastLocation)
    }

    fun onJoin() {
        setLastLocation(player.bukkitPlayer.location)
    }

    fun onQuit() {
        player.update(lastLocation) // 退出的时候保存一下
    }

    fun teleportRequest(playerName: String, type: TeleportType) {
        val sender = player.core.playerInfo
        val request = TeleportRequestMessage(sender, playerName, type)
        submit {
            val result = Broker.invokeSync<Int>(request)
            when (result) {
                TeleportRequestMessage.SUCCESS -> {
                    MessageType.ALLOW.sendMessage(
                        player.bukkitPlayer,
                        "你向玩家 $playerName 发送了传送请求"
                    )
                }

                TeleportRequestMessage.PLAYER_NOT_ONLINE -> {
                    MessageType.FAILED.sendMessage(player.bukkitPlayer, "玩家 $playerName 不在线")
                }

                // 已经发送过好友请求了
                TeleportRequestMessage.REQUEST_ALREADY_EXISTS -> {
                    MessageType.FAILED.sendMessage(
                        player.bukkitPlayer,
                        "你已经向玩家 $playerName 发送了传送请求, 请稍后再试"
                    )
                }

                // 对方暂时不可用
                TeleportRequestMessage.PLAYER_NOT_AVAILABLE -> {
                    MessageType.FAILED.sendMessage(
                        player.bukkitPlayer,
                        "玩家 $playerName 暂时无法回应你的传送请求, 请稍后再试"
                    )
                }

                // 对方关闭了好友传送
                TeleportRequestMessage.REQUEST_DISABLED -> {
                    MessageType.FAILED.sendMessage(
                        player.bukkitPlayer,
                        "玩家 $playerName 关闭了传送请求"
                    )
                }

                // 好友快捷传送
                TeleportRequestMessage.FRIEND_DIRECT -> {
                    MessageType.INFO.sendMessage(player.bukkitPlayer, "传送将在 3 秒后开始.")
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                        teleport(request.receiver, TeleportType.TPA)
                    }, 60)
                }
            }
        }
    }

    fun teleportResponse(playerName: String, accept: Boolean) {
        val sender = player.core.playerInfo

        val request = requests[playerName]
        if (request == null) {
            MessageType.FAILED.sendMessage(
                player.bukkitPlayer,
                "没有来自玩家 $playerName 的传送请求"
            )
            return
        }
        val type = TeleportType.values()[request.type]
        val response = TeleportResponseMessage(sender, playerName, type, accept)
        submit {
            val result = Broker.invokeSync<Int>(response)
            when (result) {
                TeleportResponseMessage.SUCCESS -> {
                    if (accept) {
                        MessageType.ALLOW.sendMessage(
                            player.bukkitPlayer,
                            "你接受了玩家 $playerName 的传送请求"
                        )
                        if (type == TeleportType.TPAHERE) {
                            MessageType.INFO.sendMessage(player.bukkitPlayer, "传送将在 3 秒后开始.")
                            player.teleport.teleport(playerName, TeleportType.TPAHERE)
                        }
                    } else {
                        MessageType.ALLOW.sendMessage(
                            player.bukkitPlayer,
                            "你拒绝了玩家 $playerName 的传送请求"
                        )
                    }
                    submit(async = false) {
                        requests.remove(playerName) // 删除请求
                    }
                }

                TeleportRequestMessage.PLAYER_NOT_ONLINE -> {
                    MessageType.FAILED.sendMessage(player.bukkitPlayer, "玩家 $playerName 不在线")
                    submit(async = false) {
                        requests.remove(playerName) // 删除请求
                    }
                }

                TeleportResponseMessage.PLAYER_NOT_AVAILABLE -> {
                    MessageType.FAILED.sendMessage(
                        player.bukkitPlayer,
                        "暂时无法与玩家 ${playerName}进行传送, 请稍后再试"
                    )
                }
            }
        }
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

    fun addRequest(sender: String, type: TeleportType) {
        requests[sender] = TeleportRequestCache().also {
            it.sender = sender
            it.type = type.ordinal
            it.expireAt = System.currentTimeMillis() + expire
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
