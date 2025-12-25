package server.auth.schema

import com.google.gson.JsonObject
import org.jetbrains.exposed.sql.Table
import server.common.util.database.initSchema


/**
 * @author Nipuru
 * @since 2025/12/24 20:22
 */
object SheetTable : Table() {
    override val tableName: String = "tb_sheet"
    val id = integer("id").autoIncrement()
    val name = text("table_name")
    val data = text("data")

    init {
        initSchema()
    }
}

data class SheetData(val id: Int = 0, var name: String, var data: JsonObject)