package server.bukkit.command.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import server.bukkit.command.suggestion
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.gameplay.player.GamePlayerManager
import server.bukkit.nms.message


/**
 * @author Nipuru
 * @since 2025/08/05 17:30
 */
object GamePlayerArgument : ArgumentType<GamePlayer, String>(StringArgumentType.string()) {

    private val ERROR_PLAYER_NOT_FOUND = DynamicCommandExceptionType { playerName ->
        "玩家不存在: '$playerName'".message()
    }

    override fun <S : Any> convert(playerName: String, reader: StringReader, source: S): GamePlayer {
        val player = GamePlayerManager.getPlayers().firstOrNull {
            it.name == playerName
        }
        return player ?: throw ERROR_PLAYER_NOT_FOUND.createWithContext(reader, playerName)
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder) = suggestion(context, builder) {
        GamePlayerManager.getPlayers().map { it.name }
    }
}