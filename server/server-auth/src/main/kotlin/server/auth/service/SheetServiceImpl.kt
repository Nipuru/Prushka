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
import server.auth.schema.SheetTable
import server.common.logger.Logger
import server.common.service.SheetService


/**
 * @author Nipuru
 * @since 2025/12/11 22:37
 */
object SheetServiceImpl : SheetService {

    private val byTableName = mutableMapOf<String, JsonArray>() // tableName -> List<JsonObject>
    private val byId = mutableMapOf<Int, JsonObject>()

    init {
        transaction {
            SheetTable.selectAll().orderBy(SheetTable.id, SortOrder.ASC).forEach {
                val tableName = it[SheetTable.name]
                val id = it[SheetTable.id]
                val data = JsonParser.parseString(it[SheetTable.data]).asJsonObject
                put(id, tableName, data)
            }
        }
    }


    fun insertSheet(tableName: String, data: JsonObject) {
        transaction {
            val id = SheetTable.insertReturning {
                it[this.name] = tableName
                it[this.data] = data.toString()
            }.first()[SheetTable.id]
            put(id, tableName, data)
        }
    }

    fun updateSheet(id: Int, data: JsonObject) {
        val old = byId[id]
        if (old == null) {
            Logger.error("Sheet $id not found, data $data")
            return
        }
        // 这里因为没有替换对象的操作所以可以同时改变 byTableName byId
        old.asMap().clear()
        old.asMap().putAll(data.asMap())
        transaction {
            SheetTable.update(
                where = { SheetTable.id.eq(id) },
                body = { it[SheetTable.data] = data.toString() }
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
                byTableName[tableName]?.asList()?.removeIf { it === data }
            }
        }
    }

    fun getSheetList(tableName: String): JsonArray? {
        return byTableName[tableName]
    }

    override fun getSheets(): Map<String, String> {
        return byTableName.mapValues { it.value.toString() }
    }

    private fun put(id: Int, tableName: String, data: JsonObject) {
        // 确保两个集合的内对象相同
        byTableName.getOrPut(tableName) { JsonArray() }.add(data)
        byId[id] = data
    }
}