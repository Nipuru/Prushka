package server.bukkit.command.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import server.bukkit.command.locale
import server.bukkit.command.suggestion
import server.bukkit.nms.message
import server.bukkit.util.CommandArgumentType
import server.common.sheet.Sheet
import server.common.sheet.StRank
import server.common.sheet.getAllStRank


/**
 * @author Nipuru
 * @since 2025/08/05 17:30
 */
object RankArgument : CommandArgumentType<StRank, String>(StringArgumentType.string()) {

    private val ERROR_RANK_NOT_FOUND = DynamicCommandExceptionType { rankName ->
        "称号不存在: '$rankName'".message()
    }

    override fun <S : Any> convert(rankName: String, reader: StringReader, source: S): StRank {
        source as CommandSourceStack
        val cfg = Sheet.getAllStRank(source.locale).values.firstOrNull { it.name == rankName }
        return cfg ?: throw ERROR_RANK_NOT_FOUND.createWithContext(reader, rankName)
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder) = suggestion(context, builder) {
        context as CommandSourceStack
        Sheet.getAllStRank(context.locale).values.map { it.name }
    }
}