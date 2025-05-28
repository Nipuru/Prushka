package top.nipuru.prushka.auth.http

import io.ktor.server.routing.*
import top.nipuru.prushka.auth.http.admin.adminRouting
import top.nipuru.prushka.auth.http.pay.payRouting


/**
 * @author Nipuru
 * @since 2025/01/22 15:55
 */
fun Route.rootRouting() {
    adminRouting()
    payRouting()
}
