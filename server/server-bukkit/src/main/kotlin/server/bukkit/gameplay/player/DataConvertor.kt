package server.bukkit.gameplay.player

import server.common.message.PlayerDataMessage.TableInfo
import java.io.IOException
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.math.BigDecimal
import java.util.*
import kotlin.reflect.KProperty1

@PublishedApi
internal object DataConvertor {

    private val cache = mutableMapOf<Class<*>, DataClassCache>()
    // 参考 server-database\src\main\kotlin\server\database\schema\PlayerDataTable.kt
    private val supportedTypes = listOf(
        Boolean::class, Byte::class, Short::class, Int::class, Long::class, Float::class, Double::class,
        String::class, Char::class, ByteArray::class, BigDecimal::class, UUID::class
    )

    fun preload(request: TableInfos, dataClass: Class<*>) {
        val dataClassCache = getOrCache(dataClass)
        if (dataClassCache.isCache) return
        val fields = mutableListOf<Triple<String, Class<*>, Any>>()
        dataClassCache.tableFields.forEach{
            val name = it.key
            val genericType = it.value.genericType
            var type = it.value.type
            if (genericType is ParameterizedType) {
                if (genericType.rawType != List::class.java) error("ParameterizedType field must be of type List<T>")
                type = genericType.actualTypeArguments[0] as Class<*>
            }
            if (!supportedTypes.contains(type.kotlin)) {
                error("Unsupported type: $type")
            }
            val default = it.value.get(dataClassCache.defaultInstance)
            if (default == null) error("Field $name must have a default value")

            fields += Triple(name, type as Class<*>, default)
        }
        val tableInfo = TableInfo(
            dataClassCache.tableName,
            dataClassCache.autoCreate,
            fields,
            dataClassCache.uniqueFields
        )
        request.tables.add(tableInfo)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> unpack(tables: Map<String, List<Any>>, dataClass: Class<T>): T? {
        val dataClassCache = getOrCache(dataClass)

        val values = tables[dataClassCache.tableName]
        if (values == null || values.size == 1) {
            return null
        }
        val fields = (values[0] as String).split(";").map { dataClassCache.fields[it] }
        if (values.size > fields.size) {
            throw IOException("Too many results for " + dataClass.name)
        }
        val instance = dataClassCache.constructor.newInstance() as T
        for (i in 1 until values.size) {
            val field = fields[i - 1]
            if (field != null) {
                field[instance] = values[i]
            }
        }
        return instance
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> unpackList(tables: Map<String, List<Any>>, dataClass: Class<T>): List<T> {
        val dataClassCache = getOrCache(dataClass)
        val values = tables[dataClassCache.tableName]
        if (values == null || values.size == 1) {
            return emptyList()
        }
        val fields = (values[0] as String).split(";").map { dataClassCache.fields[it] }
        val result = mutableListOf<T>()
        var instance = dataClassCache.constructor.newInstance() as T
        for (i in 1 until values.size) {
            val fieldIndex = (i - 1) % fields.size
            val field = fields[fieldIndex]
            if (field != null) {
                field[instance] = values[i]
            }
            if (fieldIndex == fields.size - 1) {
                result.add(instance)
                instance = dataClassCache.constructor.newInstance() as T
            }
        }
        return result
    }

    fun pack(tables: MutableMap<String, MutableList<Any>>, data: Any) {
        val dataClassCache = getOrCache(data.javaClass)
        val values = tables.getOrPut(dataClassCache.tableName) {
            mutableListOf(dataClassCache.fields.keys.joinToString(";"))
        }
        for (field in dataClassCache.fields.values) {
            values.add(field[data])
        }
    }

    fun <T : Any> getProperty(data: Any, properties: Array<out KProperty1<T, *>>): Array<String> {
        if (properties.isEmpty()) {
            val cache = getOrCache(data.javaClass)
            return cache.updateFields.keys.toTypedArray()
        }
        return properties.map { it.name }.toTypedArray()
    }

    fun getOrCache(dataClass: Class<*>): DataClassCache {
        var dataClassCache = cache[dataClass]
        if (dataClassCache == null) {
            dataClassCache = createCache(dataClass)
            cache[dataClass] = dataClassCache
        }
        return dataClassCache
    }

    private fun createCache(dataClass: Class<*>): DataClassCache {
        if (!dataClass.isAnnotationPresent(Table::class.java)) {
            throw Exception("dataClass must be annotated with @Table, provided: " + dataClass.name)
        }
        val constructor: Constructor<*>
        try {
            constructor = dataClass.getDeclaredConstructor()
            constructor.setAccessible(true)
        } catch (_: NoSuchMethodException) {
            throw Exception("dataClass must have default constructor, provided: " + dataClass.name)
        }
        val table = dataClass.getAnnotation(Table::class.java)
        val uniqueFields = mutableListOf<String>() // 被 @Unique 注释的字段
        val fields = mutableMapOf<String, Field>() // 所有字段
        val tableFields = mutableMapOf<String, Field>() // 除 @Temp 之外的字段
        val updateFields = mutableMapOf<String, Field>() // 包含在 tableFields 中的非 Unique 字段
        val fieldNames = mutableMapOf<String, String>()
        for (field in dataClass.declaredFields) {
            field.isAccessible = true
            val name = field.name
            if (field.isAnnotationPresent(Unique::class.java)) {
                uniqueFields.add(name)
            } else {
                updateFields[name] = field
            }
            tableFields[name] = field
            fields[name] = field
            fieldNames[field.name] = name
        }
        return DataClassCache(
            tableName = table.name,
            autoCreate = table.autoCreate,
            isCache = table.cache,
            uniqueFields = uniqueFields,
            fields = fields,
            tableFields = tableFields,
            updateFields = updateFields,
            constructor = constructor,
            defaultInstance = constructor.newInstance()
        )
    }

    class DataClassCache(
        val tableName: String,
        val autoCreate: Boolean,
        val isCache: Boolean,
        val uniqueFields: List<String>,
        val fields: Map<String, Field>,
        val tableFields: Map<String, Field>,
        val updateFields: Map<String, Field>,
        val constructor: Constructor<*>,
        val defaultInstance: Any
    )
}
