package server.bukkit.command.argument

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.afyer.afybroker.client.Broker
import server.common.message.PlayerInfoMessage
import server.common.service.PlayerInfoService
import java.util.concurrent.CompletableFuture


/**
 * @author Nipuru
 * @since 2025/08/05 17:30
 */
@Suppress("UnstableApiUsage")
object PlayerInfoArgument : CustomArgumentType.Converted<CompletableFuture<PlayerInfoMessage?>, String> {

    private val service = Broker.getService(PlayerInfoService::class.java)

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.string()
    }

    override fun convert(playerName: String): CompletableFuture<PlayerInfoMessage?>  {
        return CompletableFuture.supplyAsync {
            service.getByName(playerName)
        }
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        if (context.source !is CommandSourceStack) return Suggestions.empty()
        return CompletableFuture.supplyAsync {
            val names = service.completeNames(builder.remaining, 100)
            names.forEach { builder.suggest(it) }
            builder.build()
        }
    }
}