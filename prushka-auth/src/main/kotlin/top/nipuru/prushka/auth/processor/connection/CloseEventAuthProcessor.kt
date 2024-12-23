package top.nipuru.prushka.auth.processor.connection

import com.alipay.remoting.Connection
import com.alipay.remoting.ConnectionEventProcessor

class CloseEventAuthProcessor : ConnectionEventProcessor {
    override fun onEvent(remoteAddress: String, connection: Connection) {

    }
}
