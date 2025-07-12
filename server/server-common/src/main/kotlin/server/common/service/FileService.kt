package server.common.service


/**
 * @author Nipuru
 * @since 2025/07/12 17:51
 */
interface FileService {
    fun getFile(fileName: String): ByteArray

    fun saveFile(fileName: String, data: ByteArray)
}