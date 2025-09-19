package server.broker.util

import com.alipay.remoting.BizContext
import net.afyer.afybroker.server.Broker
import java.rmi.RemoteException


/**
 * @author Nipuru
 * @since 2025/09/18 23:50
 */
fun BizContext.require(type : String) {
    val clientType = Broker.getClient(this)?.type
    if (clientType != type) throw RemoteException("Invalid client type $clientType, expected $type")
}