package server.common.service


/**
 * @author Nipuru
 * @since 2025/07/12 17:52
 */
interface OfflineDataService {
    fun insert(playerId: Int, module: String, data: String, duplicateKey: String?)
}