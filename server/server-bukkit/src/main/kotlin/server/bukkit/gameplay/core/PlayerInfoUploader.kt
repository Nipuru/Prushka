package server.bukkit.gameplay.core

import net.afyer.afybroker.client.Broker
import server.bukkit.BukkitPlugin
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.time.TimeManager
import server.common.message.PlayerInfoMessage
import server.common.service.PlayerInfoService


/**
 * @author Nipuru
 * @since 2025/09/05 16:42
 */
class PlayerInfoUploader(val player: GamePlayer) {

    private val service = Broker.getService(PlayerInfoService::class.java)
    private var lastData: PlayerInfoMessage? = null   // 上次上传的游戏信息
    private var lastUpdate = 0                       // 上次游戏信息上传的系统时间（tick）
    private val interval = 20    // 间隔最少 20tick

    fun upload(force: Boolean) {
        val serverTick = TimeManager.serverTick
        if (!force && serverTick - lastUpdate < interval)  return
        val data = player.core.playerInfo
        if (lastData == data) return
        lastData = data
        lastUpdate = serverTick
        BukkitPlugin.bizThread.submit {
            service.insertOrUpdate(data)
        }
    }
}