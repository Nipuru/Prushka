package top.nipuru.prushka.common.message.database

import java.io.Serializable


/**
 * @author Nipuru
 * @since 2024/11/28 11:02
 */
class LoadFileRequest(val filename: String)
class SaveFileRequest(val filename: String, val data: ByteArray) : Serializable