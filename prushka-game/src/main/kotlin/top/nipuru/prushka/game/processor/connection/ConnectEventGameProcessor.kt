package top.nipuru.prushka.game.processor.connection

import com.alipay.remoting.Connection
import com.alipay.remoting.ConnectionEventProcessor
import net.afyer.afybroker.client.Broker
import top.nipuru.prushka.common.message.GetTimeRequest
import top.nipuru.prushka.game.gameplay.player.GamePlayers
import top.nipuru.prushka.game.logger.logger
import top.nipuru.prushka.game.time.TimeManager
import top.nipuru.prushka.game.util.submit


/**
 * @author Nipuru
 * @since 2024/11/28 10:42
 */
class ConnectEventGameProcessor : ConnectionEventProcessor {
    override fun onEvent(s: String, connection: Connection) {
        // 初始化定时器
        TimeManager.apply {
            logger.info("Get time from shared server...")
            debugTime = Broker.invokeSync(GetTimeRequest())
            newDayFunc = {
                submit(async = false) {
                    GamePlayers.onNewDay(it)
                }
            }
            init()
        }
    }
}
