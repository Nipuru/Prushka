package server.bukkit.gameplay.player

import server.common.message.FieldMessage
import server.common.message.TableInfo
import java.io.Serializable

interface Data


class TableInfos : Serializable {
    val tables = mutableListOf<TableInfo>()
}

class DataInfo(val tables: MutableMap<String, MutableList<List<FieldMessage>>>) {

    inline fun <reified T : Data> unpack(): T? = DataConvertor.unpack(tables)

    inline fun <reified T : Data> unpackList(): List<T> = DataConvertor.unpackList(tables)

    fun <T: Data> pack(data: T) = DataConvertor.pack(this.tables, data)
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


inline fun <reified T> TableInfos.preload() = DataConvertor.preload<T>(this)