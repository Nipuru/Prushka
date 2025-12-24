package server.auth.http

import io.ktor.server.routing.*
import server.auth.util.success
import server.common.sheet.Sheet


/**
 * @author Nipuru
 * @since 2025/12/24 17:21
 */
suspend fun RoutingContext.getSheetMetadata() {
    val metadata = Sheet.getAllMetadata()
    call.success("获取成功", metadata)
}