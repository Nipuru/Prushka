package server.common.service


/**
 * @author Nipuru
 * @since 2025/12/11 22:35
 */
interface SheetService {

    /**
     * 获取游戏配置表
     */
    fun getSheets(): Map<String, String>
}