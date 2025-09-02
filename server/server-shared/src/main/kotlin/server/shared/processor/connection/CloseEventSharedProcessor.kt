package server.shared.processor.connection

import com.alipay.remoting.Connection
import com.alipay.remoting.ConnectionEventType
import net.afyer.afybroker.core.util.ConnectionEventTypeProcessor

class CloseEventSharedProcessor : ConnectionEventTypeProcessor {
    override fun getType() = ConnectionEventType.CLOSE
    override fun onEvent(remoteAddress: String, connection: Connection) {
    }
}
