package top.nipuru.prushka.auth.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import java.util.*


/**
 * @author Nipuru
 * @since 2025/01/10 14:37
 */
object JWTUtil {
    // auth 是个单体 每次启动使用随机字符串作为秘钥
    private var secret = UUID.randomUUID().toString()
    private var issuer = "auth"
    private var algorithm = Algorithm.HMAC256(secret)

    fun makeToken(username: String): String {
        return JWT.create()
            .withClaim("username", username)
            .withIssuer(issuer)
            .withAudience()
            .sign(algorithm)
    }

    fun makeVerifier(): JWTVerifier {
        return JWT.require(algorithm)
            .withIssuer(issuer)
            .build()
    }
}