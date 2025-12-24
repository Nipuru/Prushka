package server.common.util.database

import org.jetbrains.exposed.sql.ArrayColumnType
import org.jetbrains.exposed.sql.BasicBinaryColumnType
import org.jetbrains.exposed.sql.BooleanColumnType
import org.jetbrains.exposed.sql.ByteColumnType
import org.jetbrains.exposed.sql.CharacterColumnType
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.DecimalColumnType
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.FloatColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.ShortColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.UUIDColumnType
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.math.MathContext
import java.util.UUID
import kotlin.reflect.KClass

/**
 * @author Nipuru
 * @since 2025/06/11 17:22
 */
fun Table.initSchema() {
    transaction {
        SchemaUtils.create(this@initSchema)
        SchemaUtils.createMissingTablesAndColumns(this@initSchema)
    }
}

@Suppress("UNCHECKED_CAST")
fun Table.registerColumn(name: String, clazz: KClass<*>, default: Any): Column<*> {
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