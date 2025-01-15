package top.nipuru.prushka.auth.web.admin

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import top.nipuru.prushka.auth.web.admin.auth.authRouting
import top.nipuru.prushka.auth.web.admin.auth.loginRouting
import top.nipuru.prushka.auth.web.admin.charge.chargeRouting
import top.nipuru.prushka.auth.web.admin.player.playerRouting
import top.nipuru.prushka.auth.web.admin.whitelist.whitelistRouting


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