package server.bukkit.util

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor


/**
 * @author Nipuru
 * @since 2025/09/20 12:34
 */
fun <T> Executor.completeFuture(block: () -> T): CompletableFuture<T> {
    return CompletableFuture.supplyAsync({ block() }, this)
}