package server.bukkit.command.argument

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Bukkit
import org.bukkit.World
import java.util.concurrent.CompletableFuture


/**
 * @author Nipuru
 * @since 2025/08/05 16:53
 */
@Suppress("UnstableApiUsage")
object WorldArgument : ArgumentType<World> {

    private val ERROR_UNKNOWN_WORLD = DynamicCommandExceptionType { worldName ->
        LiteralMessage("World not found: '$worldName'")
    }

    fun getWorld(context: CommandContext<CommandSourceStack>, name: String): World {
        return context.getArgument(name, World::class.java)
    }

    override fun parse(reader: StringReader): World {
        val worldName = reader.readUnquotedString()
        return Bukkit.getWorld(worldName) ?: throw ERROR_UNKNOWN_WORLD.create(worldName)
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        if (context.source !is CommandSourceStack) return Suggestions.empty()
        Bukkit.getWorlds().map { it.name }.forEach {
            builder.suggest(it)
        }
        return builder.buildFuture()
    }
}