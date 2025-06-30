package server.common.message.log

import jdk.jfr.StackTrace
import java.io.Serializable


/**
 * @author Nipuru
 * @since 2025/06/30 18:15
 */
class ReportErrorMessage(
    val serverType: String,
    val serverName: String,
    val errorMessage: String,
    val stackTrace: String,
    val time: Long
) : Serializable