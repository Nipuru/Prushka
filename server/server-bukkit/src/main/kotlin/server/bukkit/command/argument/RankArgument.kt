package server.bukkit.command.argument

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import server.bukkit.command.locale
import server.bukkit.command.suggestion
import server.bukkit.gameplay.player.GamePlayerManager
import server.bukkit.nms.message
import server.common.sheet.Sheet
import server.common.sheet.StRank
import server.common.sheet.getAllStRank


/**
 * @author Nipuru
 * @since 2025/08/05 17:30
 */
@Suppress("UnstableApiUsage")
object RankArgument : CustomArgumentType.Converted<StRank, String> {

    private val ERROR_RANK_NOT_FOUND = DynamicCommandExceptionType { rankName ->
        "称号不存在: '$rankName'".message()
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.string()
    }

    override fun convert(nativeType: String): StRank {
        throw UnsupportedOperationException()
    }

    override fun <S : Any> convert(rankName: String, source: S): StRank {
        source as CommandSourceStack
        val cfg = Sheet.getAllStRank(source.locale).values.firstOrNull { it.name == rankName }
        return cfg ?: throw ERROR_RANK_NOT_FOUND.create(rankName)
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder) = suggestion(context, builder) {
        GamePlayerManager.getPlayers().map { it.name }
    }
}