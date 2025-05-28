package top.nipuru.prushka.server.auth.http.admin

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import top.nipuru.prushka.server.auth.http.admin.auth.authRouting
import top.nipuru.prushka.server.auth.http.admin.auth.loginRouting
import top.nipuru.prushka.server.auth.http.admin.charge.chargeRouting
import top.nipuru.prushka.server.auth.http.admin.player.playerRouting
import top.nipuru.prushka.server.auth.http.admin.whitelist.whitelistRouting


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