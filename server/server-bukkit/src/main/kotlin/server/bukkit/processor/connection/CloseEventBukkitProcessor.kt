package server.bukkit.processor.connection

import com.alipay.remoting.Connection
import com.alipay.remoting.ConnectionEventType
import net.afyer.afybroker.core.util.ConnectionEventTypeProcessor

class CloseEventBukkitProcessor : ConnectionEventTypeProcessor {

    override fun getType() = ConnectionEventType.CLOSE

    override fun onEvent(remoteAddress: String, connection: Connection) {
    }
}
