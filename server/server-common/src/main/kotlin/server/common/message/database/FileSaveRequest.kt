package server.common.message.database

import java.io.Serializable


/**
 * @author Nipuru
 * @since 2025/06/11 15:24
 */
class FileSaveRequest(val filename: String, val data: ByteArray) : Serializable