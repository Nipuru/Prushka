package server.common.sheet

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder

object Sheet {
    private var isLoad = false
    private val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
    private val metadataMap = loadAllMetadata()

    @Suppress("UNCHECKED_CAST")
    fun load(sheets: Map<String, String>) {
        for (metadata in metadataMap.values) {
            val holder = Class.forName(metadata.holderClass).getDeclaredField("INSTANCE").get(null) as SheetHolder<Any>
            holder.clear()
            val json = sheets[metadata.tableName] ?: loadFromMetadataJson(metadata.tableName)
            if (json == null) error("Sheet data for '${metadata.tableName}' not found")

            val values = gson.fromJson<Array<Any>>(json, holder.type().arrayType())
            for (value in values) {
                holder.put(value)
            }
        }
        isLoad = true
    }

    fun getAllMetadata(): Collection<SheetMetadata> {
        return metadataMap.values
    }

    fun getMetadata(name: String): SheetMetadata? {
        return metadataMap[name]
    }

    fun check() {
        if (!isLoad) throw IllegalStateException("Sheet not loaded")
    }

    private fun loadFromMetadataJson(name: String): String? {
        val metadata = metadataMap[name] ?: return null

        // 将 data 数组转换为对象数组的 JSON 字符串
        val fields = metadata.fields
        val jsonArray = StringBuilder("[")

        metadata.data.forEachIndexed { index, row ->
            if (index > 0) jsonArray.append(",")
            jsonArray.append("{")
            row.forEachIndexed { fieldIndex, value ->
                if (fieldIndex < fields.size) {
                    if (fieldIndex > 0) jsonArray.append(",")
                    val fieldName = fields[fieldIndex].name
                    jsonArray.append(gson.toJson(fieldName))
                    jsonArray.append(":")
                    jsonArray.append(gson.toJson(value))
                }
            }
            jsonArray.append("}")
        }
        jsonArray.append("]")
        return jsonArray.toString()
    }

    private fun loadAllMetadata(): Map<String, SheetMetadata> {
        val metadataMap = mutableMapOf<String, SheetMetadata>()
        val resourcePath = "sheet_metadata.json"
        val inputStream = javaClass.classLoader.getResourceAsStream(resourcePath) ?: return emptyMap()

        val metadataJson = inputStream.bufferedReader().use { it.readText() }
        val metadata = gson.fromJson(metadataJson, Array<SheetMetadata>::class.java) ?: emptyArray()
        metadata.forEach { metadataMap[it.tableName] = it }
        return metadataMap
    }
}

interface SheetHolder<T> {
    fun put(value: T)
    fun clear()
    fun type(): Class<T>
}

data class SheetMetadata(
    val tableName: String,
    val holderClass: String,
    val indexes: IndexInfo,
    val unique: List<List<String>>,
    val fields: List<FieldInfo>,
    val data: List<List<Any>>
)

data class IndexInfo(
    val key: String?,
    val vkey: String?,
    val akey: String?,
    val subkey: String?
)

data class FieldInfo(
    val name: String,
    val type: String,
    val comment: String,
    val resolvedType: String?
)