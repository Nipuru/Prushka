package server.auth.http

import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.auth.constant.HttpStatus
import server.auth.util.Result


/**
 * @author Nipuru
 * @since 2025/01/22 15:55
 */
fun Route.configureRouting() {
    get("/") {
        call.respond(Result(HttpStatus.SUCCESS, "Hello World!"))
    }
}
