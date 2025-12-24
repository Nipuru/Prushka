package server.database.schema

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import server.common.message.PlayerDataMessage.TableInfo
import server.common.util.database.registerColumn


/**
 * @author Nipuru
 * @since 2024/12/18 15:23
 */
class PlayerDataTable(tableInfo: TableInfo) : Table() {
    override val tableName = tableInfo.tableName
    val playerId = integer("player_id")
    val columnMap = mutableMapOf<String, Column<*>>()
    override val primaryKey: PrimaryKey

    init {
        for ((name, clazz, default) in tableInfo.fields) {
            val kClass = clazz.kotlin
            val column = registerColumn(name, kClass, default)
            columnMap[name] = column
        }
        val uniqueColumn = tableInfo.uniqueKeys.map { column(it) }.toTypedArray()
        primaryKey = PrimaryKey(playerId, *uniqueColumn)
    }

    @Suppress("UNCHECKED_CAST")
    fun column(name: String): Column<Any> {
        val column = columnMap[name]!!
        return column as Column<Any>
    }

    @Suppress("UNCHECKED_CAST")
    fun setColumn(statement: UpdateBuilder<*>, name: String, value: Any) {
        val column = columnMap[name]!!
        statement[column as Column<Any>] = value
    }

    fun getColumn(row: ResultRow, name: String): Any {
        val column = columnMap[name]!!
        return row[column]!!
    }
}
