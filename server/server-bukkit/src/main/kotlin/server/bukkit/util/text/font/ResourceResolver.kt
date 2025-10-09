package server.bukkit.util.text.font

import java.io.InputStream


/**
 * @author Nipuru
 * @since 2025/10/09 17:43
 */
fun interface ResourceResolver {
    fun resolve(fileName: String): InputStream?
}