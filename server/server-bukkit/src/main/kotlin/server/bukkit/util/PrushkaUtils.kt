package server.bukkit.util

import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.bukkit.Bukkit
import server.bukkit.logger.logger
import server.bukkit.plugin
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

