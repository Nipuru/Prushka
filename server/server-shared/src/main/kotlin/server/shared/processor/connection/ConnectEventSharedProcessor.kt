package server.shared.processor.connection

import com.alipay.remoting.Connection
import com.alipay.remoting.ConnectionEventType
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.core.util.ConnectionEventTypeProcessor
import server.common.message.DebugTimeNotify


/**
 * @author Nipuru
 * @since 2024/11/28 10:42
 */
class ConnectEventSharedProcessor : ConnectionEventTypeProcessor {
    override fun getType() = ConnectionEventType.CONNECT
    override fun onEvent(s: String, connection: Connection) {
        // 连接成功后广播时间
        val debugTime = server.shared.time.TimeManager.debugTime()
        Broker.oneway(DebugTimeNotify(debugTime))
    }
}
