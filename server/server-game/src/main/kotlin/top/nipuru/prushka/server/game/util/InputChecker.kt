package top.nipuru.prushka.server.game.util

import java.util.regex.Pattern


/**
 * @author Nipuru
 * @since 2024/11/20 10:21
 */
private val tagPattern = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>")    // 简单标签的正则

fun hasTag(string: String): Boolean {
    return tagPattern.matcher(string).find()
}
