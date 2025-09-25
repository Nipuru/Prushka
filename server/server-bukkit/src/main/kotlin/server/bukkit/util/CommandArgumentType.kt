package server.bukkit.util

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import java.util.concurrent.CompletableFuture


/**
 * @author Nipuru
 * @since 2025/09/20 16:45
 */
abstract class CommandArgumentType<T : Any, N : Any>(val type: ArgumentType<N>) : CustomArgumentType<T, N> {

    final override fun parse(reader: StringReader): T =
        throw UnsupportedOperationException()

    final override fun getNativeType(): ArgumentType<N> = type

    final override fun <S : Any> parse(reader: StringReader, source: S): T =
        this.convert(this.getNativeType().parse(reader), reader, source)


    abstract fun <S : Any> convert(nativeType: N, reader: StringReader, source: S): T

    abstract override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions>
}