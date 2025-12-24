package server.auth.http

import com.google.gson.JsonObject
import io.ktor.server.request.receive
import io.ktor.server.routing.*
import io.ktor.server.util.getOrFail
import server.auth.service.SheetServiceImpl
import server.auth.util.success
import server.common.sheet.Sheet


/**
 * @author Nipuru
 * @since 2025/12/24 17:21
 */
suspend fun RoutingContext.getSheetMetadata() {
    val metadata = Sheet.getAllMetadata()
    call.success(data = metadata)
}

suspend fun RoutingContext.getSheetList() {
    val tableName = call.parameters.getOrFail<String>("tableName")
    val current = call.parameters["current"]?.toIntOrNull() ?: 1
    val pageSize = call.parameters["pageSize"]?.toIntOrNull() ?: 10

    val offset = (current - 1) * pageSize
    val tableList = SheetServiceImpl.getSheetList(tableName)?.asList() ?: emptyList()
    val pageData = if (offset + pageSize >= tableList.size) {
        tableList.subList(offset.coerceAtMost(tableList.size), tableList.size)
    } else {
        tableList.subList(offset, offset + pageSize)
    }
    call.success(data = PageData(pageData, tableList.size))
}

suspend fun RoutingContext.deleteSheet() {
    val id = call.parameters.getOrFail<Int>("id")
    SheetServiceImpl.deleteSheet(id)
    call.success()
}

suspend fun RoutingContext.insertSheet() {
    val tableName = call.parameters.getOrFail<String>("tableName")
    val data = call.receive<JsonObject>()
    SheetServiceImpl.insertSheet(tableName, data)
    call.success()
}

suspend fun RoutingContext.updateSheet() {
    val id = call.parameters.getOrFail<Int>("id")
    val data = call.receive<JsonObject>()
    SheetServiceImpl.updateSheet(id, data)
    call.success()
}
