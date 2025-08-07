package server.bukkit.command.argument

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import server.bukkit.nms.message
import server.common.message.PlayerInfoMessage
import java.util.concurrent.CompletableFuture


/**
 * @author Nipuru
 * @since 2025/08/05 17:30
 */
object PlayerInfoArgument : ArgumentType<PlayerInfoMessage> {

    private val ERROR_PLAYER_NOT_FOUND = DynamicCommandExceptionType { playerName ->
        "玩家不存在: '$playerName'".message()
    }

    override fun parse(reader: StringReader): PlayerInfoMessage {
        val playerName = reader.readUnquotedString()
        throw ERROR_PLAYER_NOT_FOUND.create(playerName)
    }

    override fun <S : Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        return super.listSuggestions(context, builder)
    }
}