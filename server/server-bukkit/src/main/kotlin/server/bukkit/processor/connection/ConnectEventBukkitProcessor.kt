package server.bukkit.processor.connection

import com.alipay.remoting.Connection
import com.alipay.remoting.ConnectionEventProcessor
import net.afyer.afybroker.client.Broker
import server.bukkit.gameplay.player.GamePlayerManager
import server.common.logger.logger
import server.bukkit.time.TimeManager
import server.bukkit.util.submit
import server.common.message.GetTimeRequest


/**
 * @author Nipuru
 * @since 2024/11/28 10:42
 */
class ConnectEventBukkitProcessor : ConnectionEventProcessor {
    override fun onEvent(s: String, connection: Connection) {
        // 初始化定时器
        TimeManager.apply {
            logger.info("Get time from shared server...")
            debugTime = Broker.invokeSync(GetTimeRequest())
            newDayFunc = {
                submit(async = false) {
                    GamePlayerManager.onNewDay(it)
                }
            }
            init()
        }
    }
}
