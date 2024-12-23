package top.nipuru.prushka.database.util

import top.nipuru.prushka.database.logger.logger
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.util.*


fun String.escape(): String {
    return this.replace("'", "''")
}

fun Statement.executeSql(sql: String) {
    logger.info("Exec sql: $sql")
    this.execute(sql)
}

@Suppress("UNCHECKED_CAST")
fun ResultSet.getObject(clazz: Class<*>, index: Int): Any {
    when (clazz) {
        Int::class.javaPrimitiveType -> {
            return this.getInt(index)
        }
        Long::class.javaPrimitiveType -> {
            return this.getLong(index)
        }
        Float::class.javaPrimitiveType -> {
            return this.getFloat(index)
        }
        Double::class.javaPrimitiveType -> {
            return this.getDouble(index)
        }
        Boolean::class.javaPrimitiveType -> {
            return this.getBoolean(index)
        }
        String::class.java -> {
            return this.getString(index)
        }
        ByteArray::class.java -> {
            return this.getBytes(index)
        }
        Array<String>::class.java -> {
            return this.getArray(index).array
        }
        IntArray::class.java -> {
            val array = this.getArray(index).array as Array<Int>
            val ints = IntArray(array.size)
            for (i in array.indices) {
                ints[i] = array[i]
            }
            return ints
        }
        LongArray::class.java -> {
            val array = this.getArray(index).array as Array<Long>
            val longs = LongArray(array.size)
            for (i in array.indices) {
                longs[i] = array[i]
            }
            return longs
        }
        FloatArray::class.java -> {
            val array = this.getArray(index).array as Array<Float>
            val floats = FloatArray(array.size)
            for (i in array.indices) {
                floats[i] = array[i]
            }
            return floats
        }
        DoubleArray::class.java -> {
            val array = this.getArray(index).array as Array<Double>
            val doubles = DoubleArray(array.size)
            for (i in array.indices) {
                doubles[i] = array[i]
            }
            return doubles
        }
        BooleanArray::class.java -> {
            val array = this.getArray(index).array as Array<Boolean>
            val booleans = BooleanArray(array.size)
            for (i in array.indices) {
                booleans[i] = array[i]
            }
            return booleans
        }
        else -> throw IllegalArgumentException("Unsupported type: $clazz")
    }
}

fun Any?.toPgSqlString(): String {
    if (this == null) {
        return "NULL"
    }

    if (this is Number) {
        return this.toString()
    }

    if (this is Boolean) {
        return if (this) "TRUE" else "FALSE"
    }

    if (this is String) {
        val value = this.toString().replace("'", "''")
        return "'$value'"
    }

    if (this.javaClass.isArray) {
        return when (this) {
            is ByteArray -> "'\\x" + this.joinToString("") { "%02X".format(it) } + "'"
            is IntArray -> "ARRAY[${this.joinToString(",")}]::INTEGER[]"
            is DoubleArray -> "ARRAY[${this.joinToString(",")}]::DOUBLE PRECISION[]"
            is LongArray -> "ARRAY[${this.joinToString(",")}]::BIGINT[]"
            is FloatArray -> "ARRAY[${this.joinToString(",")}]::REAL[]"
            else -> {
                val array = this as Array<*>
                val formattedArray = array.map { it.toPgSqlString() }
                return formattedArray.joinToString(",", prefix = "ARRAY[", postfix = "]::TEXT[]")
            }
        }
    }
    error("Unsupported type: ${this.javaClass}")
}

fun Class<*>.getSqlType(): String {
    return if (this == String::class.java) {
        "TEXT"
    } else if (this == Int::class.java || this == Int::class.javaPrimitiveType) {
        "INTEGER"
    } else if (this == Long::class.java || this == Long::class.javaPrimitiveType) {
        "BIGINT"
    } else if (this == Boolean::class.java || this == Boolean::class.javaPrimitiveType) {
        "BOOLEAN"
    } else if (this == Double::class.java || this == Double::class.javaPrimitiveType) {
        "DOUBLE PRECISION"
    } else if (this == Float::class.java || this == Float::class.javaPrimitiveType) {
        "REAL"
    } else if (this == ByteArray::class.java) {
        "BYTEA"
    } else if (this.isArray) {
        this.componentType.getSqlType() + "[]"
    } else {
        throw IllegalArgumentException("Unsupported type: " + this.simpleName)
    }
}

fun String.getSqlName(): String {
    return "\"" + this
        .replace("([a-z])([A-Z])".toRegex(), "$1_$2") // 在小写字母和大写字母之间添加下划线
        .lowercase(Locale.getDefault()) + "\""
}
