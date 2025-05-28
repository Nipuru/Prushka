package top.nipuru.prushka.server.log.schema

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.util.*
import kotlin.reflect.KClass


/**
 * @author Nipuru
 * @since 2024/12/18 15:23
 */

class LogTable(override val tableName: String, fieldTypes: Map<String, KClass<*>>) : Table() {
    private val columnMap = mutableMapOf<String, Pair<KClass<*>, Column<*>>>()

    init {
        for ((name, kClass) in fieldTypes) {
            val column = registerColumn(name, kClass)
            columnMap[column.name] = kClass to column
        }
    }

    fun insertLog(fields: Map<String, Any>) {
        insert {
            for ((k, v) in fields) {
                setColumn(it, k, v)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setColumn(statement: UpdateBuilder<*>, name: String, value: Any) {
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

    private fun registerColumn(name: String, kClass: KClass<*>): Column<*> {
        return when (kClass) {
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
            else -> error("Unsupported class ${kClass.simpleName}")
        }
    }
}
