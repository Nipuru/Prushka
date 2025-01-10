package top.nipuru.prushka.auth.admin

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import top.nipuru.prushka.auth.admin.auth.authRouting
import top.nipuru.prushka.auth.admin.auth.loginRouting
import top.nipuru.prushka.auth.admin.charge.chargeRouting
import top.nipuru.prushka.auth.admin.player.playerRouting
import top.nipuru.prushka.auth.admin.whitelist.whitelistRouting


/**
 * @author Nipuru
 * @since 2025/01/10 15:57
 */
/**
 * @author Nipuru
 * @since 2024/12/31 14:46
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