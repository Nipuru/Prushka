package server.bukkit.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import server.bukkit.BukkitPlugin.serverThread
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.gameplay.player.gamePlayer
import server.bukkit.nms.handleError
import server.bukkit.nms.message
import java.util.*
import java.util.concurrent.CompletableFuture


/**
 * @author Nipuru
 * @since 2025/08/05 17:41
 */
val ERROR_NOT_PLAYER = SimpleCommandExceptionType("只有玩家才能执行此命令".message())

fun <T : ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.requireOperator(): T = requires { it.sender.isOp }

inline fun <reified T> CommandContext<*>.getArgument(name: String): T = this.getArgument(name, T::class.java)

suspend inline fun <reified T> CommandContext<*>.getFutureArgument(name: String): T {
    val future = getArgument<CompletableFuture<T>>(name)
    return future.await()
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

@Suppress("UNCHECKED_CAST")
fun <T : ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.executes(func: suspend (CommandContext<CommandSourceStack>) -> Unit): T = this.executes {
    CoroutineScope(serverThread.asCoroutineDispatcher()).launch {
        try {
            func(it)
        } catch (e: CommandSyntaxException) {
            it.source.handleError(e)
        }
    }
    Command.SINGLE_SUCCESS
}

fun <T> ArgumentType<T>.asSuggestionProvider(): SuggestionProvider<CommandSourceStack> = SuggestionProvider { context, builder ->
    this.listSuggestions(context, builder)
}