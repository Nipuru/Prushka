package server.bukkit.gameplay.offline

fun interface OfflineDataHandler {
    /** 处理离线数据 */
    fun handle(data: String, isOnline: Boolean)
}
