package top.nipuru.prushka.shared.processor.connection

import com.alipay.remoting.Connection
import com.alipay.remoting.ConnectionEventProcessor

class CloseEventSharedProcessor : ConnectionEventProcessor {
    override fun onEvent(remoteAddress: String, connection: Connection) {
    }
}
