package server.common.service


/**
 * @author Nipuru
 * @since 2025/07/12 17:40
 */
interface LogService {

    fun log(tableName: String, fields: Map<String, Any>)

    fun reportError(serverType: String, serverName: String, errorMessage: String, stackTrace: String, time: Long)

}