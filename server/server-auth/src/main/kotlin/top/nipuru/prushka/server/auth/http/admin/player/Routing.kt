package top.nipuru.prushka.server.auth.http.admin.player

import io.ktor.server.routing.*


/**
 * 玩家管理相关
 *
 * @author Nipuru
 * @since 2024/12/31 14:55
 */
fun Route.playerRouting() = route("/player") {
    get("/kick") {

    }

    get("/ban") {

    }

    get("/unban") {

    }

    get("/mute") {

    }

    get("/unmute") {

    }
}