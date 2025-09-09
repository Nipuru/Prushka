package server.database.schema

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import server.common.message.TableInfo
import java.math.BigDecimal
import java.math.MathContext
import java.util.*
import kotlin.reflect.KClass


/**
 * @author Nipuru
 * @since 2024/12/18 15:23
 */
class PlayerDataTable(tableInfo: TableInfo) : Table() {
    override val tableName = tableInfo.tableName
    val playerId = integer("player_id")
    val columnMap = mutableMapOf<String, Pair<KClass<*>, Column<*>>>()
    override val primaryKey: PrimaryKey

    init {
        for ((name, clazz, default) in tableInfo.fields) {
            val kClass = clazz.kotlin
            val column = registerColumn(name, kClass, default)
            columnMap[name] = kClass to column
        }
        val uniqueColumn = tableInfo.uniqueKeys.map { columnMap[it]!!.second }.toTypedArray()
        primaryKey = PrimaryKey(playerId, *uniqueColumn)
    }

    @Suppress("UNCHECKED_CAST")
    fun column(name: String): Column<Any> {
        val (_, column) = columnMap[name]!!
        return column as Column<Any>
    }

    @Suppress("UNCHECKED_CAST")
    fun setColumn(statement: UpdateBuilder<*>, name: String, value: Any) {
        val (_, column) = columnMap[name]!!
        statement[column as Column<Any>] = value
    }

    fun getColumn(row: ResultRow, name: String): Any {
        val (_, column) = columnMap[name]!!
        return row[column]!!
    }

    @Suppress("UNCHECKED_CAST")
    private fun registerColumn(name: String, clazz: KClass<*>, default: Any): Column<*> {
        val columnType = when (clazz) {
            Boolean::class -> BooleanColumnType()
            Byte::class -> ByteColumnType()
            Short::class -> ShortColumnType()
            Int::class -> IntegerColumnType()
            Long::class -> LongColumnType()
            Float::class -> FloatColumnType()
            Double::class -> DoubleColumnType()
            String::class -> TextColumnType()
            Char::class -> CharacterColumnType()
            ByteArray::class -> BasicBinaryColumnType()
            BigDecimal::class -> DecimalColumnType(MathContext.DECIMAL64.precision, 20)
            UUID::class -> UUIDColumnType()
            else -> error("Unsupported column type: $clazz")
        } as ColumnType<Any>
        val fieldName = name.replace("([a-z])([A-Z])".toRegex(), "$1_$2").lowercase()
        return if (default is List<*>) {
            registerColumn(fieldName, ArrayColumnType(columnType)).default(default)
        } else {
            registerColumn(fieldName, columnType).default(default)
        }
    }
}
