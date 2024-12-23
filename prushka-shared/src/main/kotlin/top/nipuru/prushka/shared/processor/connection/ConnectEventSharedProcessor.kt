package top.nipuru.prushka.shared.processor.connection

import com.alipay.remoting.Connection
import com.alipay.remoting.ConnectionEventProcessor
import net.afyer.afybroker.client.Broker
import top.nipuru.prushka.common.message.DebugTimeNotify
import top.nipuru.prushka.shared.time.TimeManager


/**
 * @author Nipuru
 * @since 2024/11/28 10:42
 */
class ConnectEventSharedProcessor : ConnectionEventProcessor {
    override fun onEvent(s: String, connection: Connection) {
        // 连接成功后广播时间
        val debugTime = TimeManager.debugTime()
        Broker.oneway(DebugTimeNotify(debugTime))
    }
}
