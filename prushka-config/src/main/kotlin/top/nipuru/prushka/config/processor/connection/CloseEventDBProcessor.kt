package top.nipuru.prushka.config.processor.connection

import com.alipay.remoting.Connection
import com.alipay.remoting.ConnectionEventProcessor

class CloseEventDBProcessor : ConnectionEventProcessor {
    override fun onEvent(remoteAddress: String, connection: Connection) {
    }
}
