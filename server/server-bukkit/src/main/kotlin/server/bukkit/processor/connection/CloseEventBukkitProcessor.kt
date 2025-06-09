package server.bukkit.processor.connection

import com.alipay.remoting.Connection
import com.alipay.remoting.ConnectionEventProcessor

class CloseEventBukkitProcessor : ConnectionEventProcessor {
    override fun onEvent(remoteAddress: String, connection: Connection) {
    }
}
