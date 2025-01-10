package top.nipuru.prushka.auth.util

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import top.nipuru.prushka.auth.constant.HttpStatus


/**
 * @author Nipuru
 * @since 2025/01/02 14:20
 */
class Result(code: Int, msg: String, data: Any? = null)
class TableData(list: List<Any> = emptyList(), pageNum: Int = list.size, pageSize: Int = list.size, total: Int = list.size)

suspend fun ApplicationCall.success(msg: String = "操作成功", data: Any? = null) {
    this.respond(Result(HttpStatus.SUCCESS, msg, data))
}

suspend fun ApplicationCall.fail(msg: String = "操作失败") {
    this.respond(Result(HttpStatus.ERROR, msg))
}

suspend fun ApplicationCall.overdue() {
    this.respond(Result(HttpStatus.OVERDUE, "身份认证过期"))
}