package top.nipuru.prushka.database.player

import top.nipuru.prushka.common.message.database.FieldMessage
import top.nipuru.prushka.common.message.database.PlayerTransactionRequest
import top.nipuru.prushka.common.message.database.QueryPlayerRequest
import top.nipuru.prushka.common.message.database.TableInfo
import top.nipuru.prushka.database.dataSource
import top.nipuru.prushka.database.util.*
import java.sql.Connection
import java.util.concurrent.ConcurrentHashMap

object PlayerDataManager {
    private val tableInitialized = ConcurrentHashMap.newKeySet<String>()

    fun queryPlayer(request: QueryPlayerRequest): Map<String, List<List<FieldMessage>>> {
        val result = mutableMapOf<String, List<List<FieldMessage>>>()
        dataSource.connection.use { con ->
            for (tableInfo in request.tables) {
                initTable(con, tableInfo)
                var query = "select "
                val fieldNames = tableInfo.fields.keys.stream()
                    .map { fieldName: String -> fieldName.getSqlName() }.toList()
                query += java.lang.String.join(",", fieldNames)
                query += " from " + tableInfo.tableName + " where player_id=" + request.playerId

                con.createStatement().executeQuery(query).use { rs ->
                    val lists = mutableListOf<List<FieldMessage>>()
                    while (rs.next()) {
                        val fields = mutableListOf<FieldMessage>()
                        var i = 1
                        for ((key, value) in tableInfo.fields) {
                            val field = FieldMessage(key, rs.getObject(value, i++))
                            fields.add(field)
                        }
                        lists.add(fields)
                    }
                    result.put(tableInfo.tableName, lists)
                }
            }
        }
        return result
    }

    fun transaction(request: PlayerTransactionRequest) {
        dataSource.connection.use { con ->
            con.autoCommit = false
            for (delete in request.deletes) {
                val deleteSql = StringBuilder()
                deleteSql.append("delete from ").append(delete.tableName).append(" where \"player_id\"=").append(request.playerId)
                for (uniqueFields in delete.uniqueFields) {
                    deleteSql.append(" and ").append(uniqueFields.name.getSqlName()).append("=").append(uniqueFields.value.toPgSqlString())
                }
                con.createStatement().use { s ->
                    s.executeSql(deleteSql.toString())
                }
            }

            for (update in request.updates) {
                val updateSql = StringBuilder()
                updateSql.append("update ").append(update.tableName).append(" set")
                for (updateField in update.updateFields) {
                    updateSql.append(" ").append(updateField.name.getSqlName()).append("=").append(updateField.value.toPgSqlString()).append(",")
                }
                updateSql.deleteCharAt(updateSql.length - 1)
                updateSql.append(" where player_id=").append(request.playerId)
                for (uniqueFields in update.uniqueFields) {
                    updateSql.append(" and ").append(uniqueFields.name.getSqlName()).append("=").append(uniqueFields.toPgSqlString())
                }
                con.createStatement().use { s ->
                    s.executeSql(updateSql.toString())
                }
            }

            for (insert in request.inserts) {
                val insertSql = StringBuilder()
                insertSql.append("insert into ").append(insert.tableName).append("(\"player_id\",")
                for (field in insert.fields) {
                    insertSql.append(field.name.getSqlName()).append(",")
                }
                insertSql.setCharAt(insertSql.length - 1, ')')
                insertSql.append(" values(")
                insertSql.append(request.playerId).append(",")
                insert.fields
                    .map { it.value.toPgSqlString() }
                    .forEach { insertSql.append(it).append(",")}
                insertSql.setCharAt(insertSql.length - 1, ')')
                con.createStatement().use { s ->
                    s.executeSql(insertSql.toString())
                }
            }
            con.commit()
        }
    }

    private fun initTable(con: Connection, tableInfo: TableInfo) {
        if (!tableInfo.autoCreate) return
        if (!tableInitialized.add(tableInfo.tableName)) return
        val createSql = StringBuilder()
            .append("CREATE TABLE IF NOT EXISTS ")
            .append(tableInfo.tableName)
            .append("(\n")
        createSql.append("    player_id INTEGER NOT NULL,\n")
        tableInfo.fields.forEach { (fieldName, fieldType ) ->
            createSql.append("    ")
                .append(fieldName.getSqlName())
                .append(" ")
                .append(fieldType.getSqlType())
                .append(" NOT NULL,\n")
        }
        createSql.append("    CONSTRAINT pkey_").append(tableInfo.tableName).append(" PRIMARY KEY (player_id")
        if (tableInfo.uniqueKeys.isNotEmpty()) {
            val keys = tableInfo.uniqueKeys.stream().map { fieldName -> fieldName.getSqlName() }.toList()
            createSql.append(",").append(keys.joinToString(","))
        }
        createSql.append(")")
        createSql.append("\n);")
        con.createStatement().use { s ->
            s.execute(createSql.toString())
        }
    }
}
