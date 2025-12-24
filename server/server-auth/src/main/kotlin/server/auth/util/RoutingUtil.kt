package server.auth.util

import io.ktor.server.application.*
import io.ktor.server.response.*


/**
 * @author Nipuru
 * @since 2025/01/02 14:20
 */
class Result(val code: Int, val message: String, val data: Any? = null)

suspend fun ApplicationCall.success(message: String = "操作成功", data: Any? = null) {
    this.respond(Result(0, message, data))
}

suspend fun ApplicationCall.fail(message: String = "操作失败") {
    this.respond(Result(-1, message))
}

suspend fun ApplicationCall.overdue() {
    this.respond(Result(599, "身份认证过期"))
}