@file:Suppress("UnstableApiUsage")
package server.bukkit.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.gameplay.player.gamePlayer
import server.bukkit.nms.message
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException


/**
 * @author Nipuru
 * @since 2025/08/05 17:41
 */
val ERROR_NOT_PLAYER = SimpleCommandExceptionType("只有玩家才能执行此命令".message())

fun <T : ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.requireOperator(): T = requires { it.sender.isOp }

inline fun <reified T> CommandContext<*>.getArgument(name: String): T {
    return runCatching { getArgument(name, T::class.java) }.getOrElse { e ->
        // 有可能某些参数是 CompletableFuture 异步参数 但不能在主线程内调用调用 get() 会阻塞线程
        runCatching { getArgument(name, CompletableFuture::class.java) }.getOrElse { throw e }.let {
            if (Bukkit.isPrimaryThread()) error("CompletableFuture is not allowed on the server thread")
            runCatching { it.get() as T }.getOrElse {
                if (it is ExecutionException) throw it.cause ?: it
                else throw e
            }
        }
    }
}

val CommandSourceStack.gamePlayer: GamePlayer get() {
    val entity = this.executor
    if (entity !is Player) throw ERROR_NOT_PLAYER.create()
    return entity.gamePlayer
}

val CommandSourceStack.locale: Locale get() {
    if (sender is Player) {
        return (sender as Player).locale()
    }
    return Locale.SIMPLIFIED_CHINESE
}

fun <S : Any> suggestion(context: CommandContext<S>, builder: SuggestionsBuilder, suggestions: () -> List<String>): CompletableFuture<Suggestions> {
    if (context.source !is CommandSourceStack) return Suggestions.empty()
    var remaining = builder.remaining
    // 去掉引号
    for (quote in arrayOf('"', '\'')) {
        if (remaining.startsWith(quote)) {
            remaining = remaining.substring(1)
            if (remaining.endsWith(quote)) {
                remaining = remaining.substring(0, remaining.length - 1)
            }
            break
        }
    }
    suggestions.invoke().forEach {
        if (it.contains(remaining, ignoreCase = true)) {
            builder.suggest(StringArgumentType.escapeIfRequired(it))
        }
    }
    return builder.buildFuture()
}