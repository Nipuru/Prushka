package server.auth.http.admin

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import server.auth.http.admin.auth.authRouting
import server.auth.http.admin.auth.loginRouting
import server.auth.http.admin.charge.chargeRouting
import server.auth.http.admin.player.playerRouting
import server.auth.http.admin.whitelist.whitelistRouting


/**
 * @author Nipuru
 * @since 2025/01/10 15:57
 */
fun Route.adminRouting() = route("/admin") {
    loginRouting()
    authenticate {
        authRouting()
        chargeRouting()
        whitelistRouting()
        playerRouting()
    }
}