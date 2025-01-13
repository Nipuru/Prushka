package top.nipuru.prushka.common.message.log

import java.io.Serializable


/**
 * @author Nipuru
 * @since 2025/01/13 14:53
 */
class LogMessage(val tableName: String, val fields: Map<String, Any>) : Serializable