package server.bukkit.gameplay.player

import server.common.message.database.FieldMessage
import server.common.message.database.PlayerDataRequestMessage
import server.common.message.database.TableInfo
import java.io.IOException
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import kotlin.reflect.KProperty1

internal object DataConvertor {

    private val cache = mutableMapOf<Class<*>, DataClassCache>()

    fun preload(request: PlayerDataRequestMessage, dataClass: Class<*>) {
        val dataClassCache = getOrCache(dataClass)
        if (dataClassCache.isCache) return
        val fields = mutableMapOf<String, Class<*>>()
        dataClassCache.tableFields.forEach{ fields[it.key] = it.value.type }
        val tableInfo = TableInfo(dataClassCache.tableName, dataClassCache.autoCreate, fields, dataClassCache.uniqueFields)
        request.tables.add(tableInfo)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> unpack(tables: Map<String, List<List<FieldMessage>>>, dataClass: Class<T>): T? {
        val dataClassCache = getOrCache(dataClass)
        val instance = dataClassCache.constructor.newInstance() as T
        val fieldMessagesList = tables[dataClassCache.tableName]
        if (fieldMessagesList.isNullOrEmpty()) {
            return null
        }
        if (fieldMessagesList.size > 1) {
            throw IOException("Too many results for " + dataClass.name)
        }
        for (fieldMessage in fieldMessagesList[0]) {
            val field = dataClassCache.fields[fieldMessage.name] ?: continue
            field[instance] = fieldMessage.value
        }
        return instance
    }

    fun <T : Any> getProperty(data: Any, properties: Array<out KProperty1<T, *>>): Array<String> {
        if (properties.isEmpty()) {
            val cache = getOrCache(data.javaClass)
            return cache.updateFields.keys.toTypedArray()
        }
        return properties.map { it.name }.toTypedArray()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> unpackList(tables: Map<String, List<List<FieldMessage>>>, dataClass: Class<T>): List<T> {
        val dataClassCache = getOrCache(dataClass)
        val fieldMessagesList = tables[dataClassCache.tableName]
        if (fieldMessagesList.isNullOrEmpty()) {
            return emptyList()
        }
        val result = mutableListOf<T>()
        for (fieldMessages in fieldMessagesList) {
            val instance = dataClassCache.constructor.newInstance() as T
            for (fieldMessage in fieldMessages) {
                val field = dataClassCache.fields[fieldMessage.name] ?: continue
                field[instance] = fieldMessage.value
            }
            result.add(instance)
        }
        return result
    }

    fun pack(tables: MutableMap<String, MutableList<List<FieldMessage>>>, data: Any) {
        val dataClassCache = getOrCache(data.javaClass)
        val fieldMessagesList = tables.getOrPut(dataClassCache.tableName) { mutableListOf() }
        val fieldMessages = mutableListOf<FieldMessage>()
        for ((key, value) in dataClassCache.fields) {
            val fieldMessage = FieldMessage(key, value[data])
            fieldMessages.add(fieldMessage)
        }
        fieldMessagesList.add(fieldMessages)
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
        } catch (e: NoSuchMethodException) {
            throw Exception("dataClass must have default constructor, provided: " + dataClass.name)
        }
        val table = dataClass.getAnnotation(
            Table::class.java
        )
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
            table.name,
            table.autoCreate,
            table.cache,
            uniqueFields,
            fields,
            tableFields,
            updateFields,
            fieldNames,
            constructor
        )
    }

    data class DataClassCache(
        val tableName: String,
        val autoCreate: Boolean,
        val isCache: Boolean,
        val uniqueFields: List<String>,
        val fields: Map<String, Field>,
        val tableFields: Map<String, Field>,
        val updateFields: Map<String, Field>,
        val fieldNames: Map<String, String>,
        val constructor: Constructor<*>
    )
}
