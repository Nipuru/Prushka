package top.nipuru.prushka.auth.web.admin.auth

import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import top.nipuru.prushka.auth.schema.AdminUserTable
import top.nipuru.prushka.auth.util.JWTUtil


/**
 * @author Nipuru
 * @since 2024/12/31 14:46
 */
fun Route.loginRouting() = post("/login") {
    val credentials = call.receive<UserPasswordCredential>()

    val user = transaction { 
        AdminUserTable.select(
            (AdminUserTable.username eq credentials.name) and
                (AdminUserTable.password eq credentials.password)
        ).firstOrNull()
    } ?: error("用户名或密码错误")
    
    val token = JWTUtil.makeToken(credentials.name)
    call.respond(hashMapOf("token" to token))
}

fun Route.authRouting() = route("/auth") {


    get("/buttons") {

    }

    get("/menus") {

    }
}