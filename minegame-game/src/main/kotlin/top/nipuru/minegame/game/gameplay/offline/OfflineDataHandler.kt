package top.nipuru.minegame.game.gameplay.offline

fun interface OfflineDataHandler {
    /** 返回 是否成功处理离线数据 成功处理的数据应该删除  */
    fun handle(data: String, isOnline: Boolean): Boolean
}
