package top.nipuru.prushka.server.shared.processor.connection

import com.alipay.remoting.Connection
import com.alipay.remoting.ConnectionEventProcessor
import net.afyer.afybroker.client.Broker
import top.nipuru.prushka.server.common.message.DebugTimeNotify
import top.nipuru.prushka.server.shared.time.TimeManager


/**
 * @author Nipuru
 * @since 2024/11/28 10:42
 */
class ConnectEventSharedProcessor : ConnectionEventProcessor {
    override fun onEvent(s: String, connection: Connection) {
        // 连接成功后广播时间
        val debugTime = top.nipuru.prushka.server.shared.time.TimeManager.debugTime()
        Broker.oneway(DebugTimeNotify(debugTime))
    }
}
