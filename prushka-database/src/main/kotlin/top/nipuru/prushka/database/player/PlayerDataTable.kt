package top.nipuru.prushka.database.player

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import top.nipuru.prushka.common.message.database.TableInfo
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

    init {
        for ((name, clazz) in tableInfo.fields) {
            val column = registerColumn(name, clazz)
            columnMap[column.name] = clazz.kotlin to column
        }
        val uniqueColumns = tableInfo.uniqueKeys.map { columnMap[it]!!.second }.toTypedArray()
        if (uniqueColumns.isNotEmpty()) {
            uniqueIndex(columns = uniqueColumns)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun column(name: String): Column<Any> {
        val (_, column) = columnMap[name]!!
        return column as Column<Any>
    }

    @Suppress("UNCHECKED_CAST")
    fun setColumn(statement: UpdateBuilder<*>, name: String, value: Any) {
        val (type, column) = columnMap[name]!!
        statement[column as Column<Any>] = when (type) {
            BooleanArray::class -> (value as BooleanArray).toList()
            ShortArray::class -> (value as ShortArray).toList()
            IntArray::class -> (value as IntArray).toList()
            LongArray::class -> (value as LongArray).toList()
            FloatArray::class -> (value as FloatArray).toList()
            DoubleArray::class -> (value as DoubleArray).toList()
            CharArray::class -> (value as CharArray).toList()
            Array<String>::class -> (value as Array<String>).toList()
            else -> value
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getColumn(row: ResultRow, name: String): Any {
        val (type, column) = columnMap[name]!!
        val data = row[column]!!
        return when (type) {
            BooleanArray::class -> (data as List<Boolean>).toBooleanArray()
            ShortArray::class -> (data as List<Short>).toShortArray()
            IntArray::class -> (data as List<Int>).toIntArray()
            LongArray::class -> (data as List<Long>).toLongArray()
            FloatArray::class -> (data as List<Float>).toFloatArray()
            DoubleArray::class -> (data as List<Double>).toDoubleArray()
            CharArray::class -> (data as List<Char>).toCharArray()
            Array<String>::class -> (data as List<String>).toTypedArray()
            else -> data
        }
    }

    private fun registerColumn(name: String, clazz: Class<*>): Column<*> {
        return when (clazz.kotlin) {
            Boolean::class -> bool(name).default(false)
            Byte::class -> byte(name).default(0)
            Short::class -> short(name).default(0)
            Int::class -> integer(name).default(0)
            Long::class -> long(name).default(0)
            Float::class -> float(name).default(0.0F)
            Double::class -> double(name).default(0.0)
            Char::class -> char(name).default('\u0000')
            ByteArray::class -> binary(name).default(ByteArray(0))
            String::class -> text(name).default("")
            UUID::class -> uuid(name).default(UUID(0, 0))
            BooleanArray::class -> array<Boolean>(name).default(emptyList())
            ShortArray::class -> array<Short>(name).default(emptyList())
            IntArray::class -> array<Int>(name).default(emptyList())
            LongArray::class -> array<Long>(name).default(emptyList())
            FloatArray::class -> array<Float>(name).default(emptyList())
            DoubleArray::class -> array<Double>(name).default(emptyList())
            CharArray::class -> array<Char>(name).default(emptyList())
            Array<String>::class -> array<String>(name).default(emptyList())
            else -> error("Unsupported class ${clazz.simpleName}")
        }
    }
}
