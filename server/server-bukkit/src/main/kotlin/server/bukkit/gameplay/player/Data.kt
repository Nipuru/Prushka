package server.bukkit.gameplay.player

import server.common.message.database.PlayerDataRequestMessage


interface Data

fun PlayerDataRequestMessage.preload(dataClass: Class<out Data>) {
    DataConvertor.preload(this, dataClass)
}

/**
 * 表示一个数据类
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Table(
    /** 表名  */
    val name: String,
    /** 自动建表  */
    val autoCreate: Boolean = true,
    /** 是否是缓存 */
    val cache: Boolean = false
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Unique

