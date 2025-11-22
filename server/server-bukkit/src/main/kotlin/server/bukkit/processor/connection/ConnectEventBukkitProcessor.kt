package server.bukkit.processor.connection

import com.alipay.remoting.Connection
import com.alipay.remoting.ConnectionEventType
import net.afyer.afybroker.core.util.ConnectionEventTypeProcessor


/**
 * @author Nipuru
 * @since 2024/11/28 10:42
 */
class ConnectEventBukkitProcessor : ConnectionEventTypeProcessor {
    override fun getType() = ConnectionEventType.CONNECT
    override fun onEvent(s: String, connection: Connection) {

    }
}
