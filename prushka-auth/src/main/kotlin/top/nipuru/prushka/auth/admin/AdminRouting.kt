package top.nipuru.prushka.auth.admin

import io.ktor.server.routing.*


/**
 * @author Nipuru
 * @since 2024/12/31 14:46
 */
fun Route.adminRouting() = route("/admin") {
    authRouting()
    whitelistRouting()
    playerManageRouting()
    chargeRouting()
}