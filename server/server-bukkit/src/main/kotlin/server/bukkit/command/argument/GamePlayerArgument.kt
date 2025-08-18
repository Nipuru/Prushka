package server.bukkit.command.argument

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import server.bukkit.command.suggestion
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.gameplay.player.GamePlayerManager
import server.bukkit.nms.message


/**
 * @author Nipuru
 * @since 2025/08/05 17:30
 */
@Suppress("UnstableApiUsage")
object GamePlayerArgument : CustomArgumentType.Converted<GamePlayer, String> {

    private val ERROR_PLAYER_NOT_FOUND = DynamicCommandExceptionType { playerName ->
        "玩家不存在: '$playerName'".message()
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.string()
    }

    override fun convert(playerName: String): GamePlayer {
        val player = GamePlayerManager.getPlayers().firstOrNull {
            it.name == playerName
        }
        return player ?: throw ERROR_PLAYER_NOT_FOUND.create(playerName)
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder) = suggestion(context, builder) {
        GamePlayerManager.getPlayers().map { it.name }
    }
}