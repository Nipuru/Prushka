package top.nipuru.prushka.auth.admin

import io.ktor.server.routing.*


/**
 * 玩家管理相关
 *
 * @author Nipuru
 * @since 2024/12/31 14:55
 */
fun Route.playerManageRouting() = route("/player_manage") {
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