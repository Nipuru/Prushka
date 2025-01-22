package top.nipuru.prushka.auth.http.pay

import io.ktor.server.routing.*


/**
 * @author Nipuru
 * @since 2024/12/31 15:07
 */
fun Route.payRouting() = route("/pay") {
    get("/test") {

    }
}
