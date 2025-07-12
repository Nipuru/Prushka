package server.bukkit.gameplay.player


interface Data

inline fun <reified T> TableInfos.preload() {
    DataConvertor.preload<T>(this)
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

