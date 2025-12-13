package server.common.util

import server.common.logger.Logger
import server.common.sheet.Sheet
import server.common.sheet.getStI18n
import java.text.MessageFormat
import java.util.Locale


/**
 * @author Nipuru
 * @since 2025/12/13 22:13
 */
fun Locale.translateText(key: String, vararg args: Any?): String {
    val lang = toString().lowercase()
    val cfg = Sheet.getStI18n(key, lang)
    if (cfg == null) {
        Logger.warn("i18n key {} for lang {} is not found!", key, lang)
        return key
    }
    return MessageFormat.format(cfg.text, *args)
}