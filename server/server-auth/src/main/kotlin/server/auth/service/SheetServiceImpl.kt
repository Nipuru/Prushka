package server.auth.service

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertReturning
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import server.auth.schema.SheetData
import server.auth.schema.SheetTable
import server.common.logger.Logger
import server.common.service.SheetService


/**
 * @author Nipuru
 * @since 2025/12/11 22:37
 */
object SheetServiceImpl : SheetService {

    private val byTableName = mutableMapOf<String, MutableList<SheetData>>() // tableName -> List<SheetData>
    private val byId = mutableMapOf<Int, SheetData>()

    init {
        transaction {
            SheetTable.selectAll().orderBy(SheetTable.id, SortOrder.ASC).forEach {
                val tableName = it[SheetTable.name]
                val id = it[SheetTable.id]
                val data = it[SheetTable.data]
                put(id, tableName, JsonParser.parseString(data).asJsonObject)
            }
        }
    }


    fun insertSheet(data: SheetData) {
        transaction {
            val id = SheetTable.insertReturning {
                it[this.name] = tableName
                it[this.data] = data.toString()
            }.first()[SheetTable.id]
            put(id, data.name, data.data)
        }
    }

    fun updateSheet(data: SheetData) {
        val old = byId[data.id]
        if (old == null) {
            Logger.error("Sheet ${data.id} not found, data ${data.data}")
            return
        }
        // 这里因为没有替换对象的操作所以可以同时改变 byTableName byId
        old.data = data.data
        transaction {
            SheetTable.update(
                where = { SheetTable.id eq data.id },
                body = { it[SheetTable.data] = data.data.toString() }
            )
        }
    }

    fun deleteSheet(id: Int) {
        transaction {
            SheetTable.selectAll().where {
                SheetTable.id eq id
            }.firstOrNull()?.let {
                SheetTable.deleteWhere { SheetTable.id eq id }
                val tableName = it[SheetTable.name]
                val id = it[SheetTable.id]
                val data = byId[id]
                byTableName[tableName]?.removeIf { it === data }
            }
        }
    }

    fun getSheetList(tableName: String): List<SheetData> {
        return byTableName[tableName] ?: emptyList()
    }

    override fun getSheets(): Map<String, String> {
        return byTableName.mapValues {
            JsonArray().let { array ->
                it.value.forEach { sheet ->
                    array.add(sheet.data)
                }
            }.toString()
        }
    }

    private fun put(id: Int, tableName: String, data: JsonObject) {
        // 确保两个集合的内对象相同
        val data = SheetData(id, tableName, data)
        byTableName.getOrPut(tableName) { mutableListOf() }.add(data)
        byId[id] = data
    }
}