package server.bukkit.processor.connection

import com.alipay.remoting.Connection
import com.alipay.remoting.ConnectionEventProcessor
import net.afyer.afybroker.client.Broker
import server.bukkit.BukkitPlugin
import server.bukkit.gameplay.player.GamePlayerManager
import server.bukkit.time.TimeManager
import server.bukkit.util.schedule
import server.common.logger.Logger
import server.common.message.GetTimeRequest


/**
 * @author Nipuru
 * @since 2024/11/28 10:42
 */
class ConnectEventBukkitProcessor : ConnectionEventProcessor {
    override fun onEvent(s: String, connection: Connection) {
        // 初始化定时器
        TimeManager.apply {
            Logger.info("Get time from shared server...")
            debugTime = Broker.invokeSync(GetTimeRequest())
            newDayFunc = { time ->
                BukkitPlugin.schedule {
                    GamePlayerManager.onNewDay(time)
                }
            }
            init()
        }
    }
}
