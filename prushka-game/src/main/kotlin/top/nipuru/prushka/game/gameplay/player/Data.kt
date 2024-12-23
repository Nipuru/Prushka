package top.nipuru.prushka.game.gameplay.player

import top.nipuru.prushka.common.message.database.QueryPlayerRequest


interface Data

fun QueryPlayerRequest.preload(dataClass: Class<out Data>) {
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

