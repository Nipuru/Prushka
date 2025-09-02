package server.log.processor.connection

import com.alipay.remoting.Connection
import com.alipay.remoting.ConnectionEventType
import net.afyer.afybroker.core.util.ConnectionEventTypeProcessor

class CloseEventDBProcessor : ConnectionEventTypeProcessor {
    override fun getType() = ConnectionEventType.CLOSE
    override fun onEvent(remoteAddress: String, connection: Connection) {
    }
}
