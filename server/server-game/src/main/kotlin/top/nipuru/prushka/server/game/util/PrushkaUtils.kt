package top.nipuru.prushka.server.game.util

import com.google.common.util.concurrent.ThreadFactoryBuilder
import top.nipuru.prushka.server.game.logger.logger
import top.nipuru.prushka.server.game.plugin
import org.bukkit.Bukkit
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * @author Nipuru
 * @since 2024/11/21 14:17
 */

val bizThread: ExecutorService = Executors.newCachedThreadPool(
    ThreadFactoryBuilder()
        .setDaemon(false)
        .setNameFormat("Prushka-bizThread-%d")
        .build())

fun submit(async: Boolean = true, block: () -> Unit) {
    val runnable = Runnable {
        try {
            block.invoke()
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
    }
    val primaryThread = Bukkit.isPrimaryThread()
    if (async && primaryThread) {
        bizThread.submit(runnable)
    } else if (!primaryThread) {
        Bukkit.getScheduler().runTask(plugin, runnable)
    } else {
        runnable.run()
    }
}

