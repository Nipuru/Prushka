package top.nipuru.prushka.game.processor.connection

import com.alipay.remoting.Connection
import com.alipay.remoting.ConnectionEventProcessor

class CloseEventGameProcessor : ConnectionEventProcessor {
    override fun onEvent(remoteAddress: String, connection: Connection) {
    }
}
