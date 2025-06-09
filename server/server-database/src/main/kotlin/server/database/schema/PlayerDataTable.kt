package server.database.schema

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import server.common.message.database.TableInfo
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
        for ((name, clazz) in tableInfo.fields) {
            val kClass = clazz.kotlin
            val column = registerColumn(name, kClass)
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

    private fun registerColumn(name: String, clazz: KClass<*>): Column<*> {
        val fieldName = name.replace("([a-z])([A-Z])".toRegex(), "$1_$2").lowercase()
        return when (clazz) {
            Boolean::class -> bool(fieldName).default(false)
            Byte::class -> byte(fieldName).default(0)
            Short::class -> short(fieldName).default(0)
            Int::class -> integer(fieldName).default(0)
            Long::class -> long(fieldName).default(0)
            Float::class -> float(fieldName).default(0.0F)
            Double::class -> double(fieldName).default(0.0)
            Char::class -> char(fieldName).default('\u0000')
            ByteArray::class -> binary(fieldName).default(ByteArray(0))
            String::class -> text(fieldName).default("")
            UUID::class -> uuid(fieldName).default(UUID(0, 0))
            BooleanArray::class -> array<Boolean>(fieldName).default(emptyList())
            ShortArray::class -> array<Short>(fieldName).default(emptyList())
            IntArray::class -> array<Int>(fieldName).default(emptyList())
            LongArray::class -> array<Long>(fieldName).default(emptyList())
            FloatArray::class -> array<Float>(fieldName).default(emptyList())
            DoubleArray::class -> array<Double>(fieldName).default(emptyList())
            CharArray::class -> array<Char>(fieldName).default(emptyList())
            Array<String>::class -> array<String>(fieldName).default(emptyList())
            else -> error("Unsupported class ${clazz.simpleName}")
        }
    }
}
