package server.auth.http

import io.ktor.server.routing.*
import server.auth.http.admin.adminRouting
import server.auth.http.pay.payRouting


/**
 * @author Nipuru
 * @since 2025/01/22 15:55
 */
fun Route.rootRouting() {
    adminRouting()
    payRouting()
}
