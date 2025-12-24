package server.auth.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import java.util.*


/**
 * @author Nipuru
 * @since 2025/01/10 14:37
 */
object JWTUtil {
    // auth 是个单体 每次启动使用随机字符串作为秘钥
    private var secret = UUID.randomUUID().toString()
    private var issuer = "auth"
    private var audience = "game-master"
    private var algorithm = Algorithm.HMAC256(secret)

    fun makeToken(username: String): String {
        return JWT.create()
            .withClaim("username", username)
            .withIssuer(issuer)
            .withAudience(audience)
            .sign(algorithm)
    }

    fun makeVerifier(): JWTVerifier {
        return JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
    }

    fun parseAuthHeader(call : ApplicationCall): HttpAuthHeader.Single? {
        val header = call.request.header("Authorization") ?: return null
        return HttpAuthHeader.Single("Bearer", header)
    }
}