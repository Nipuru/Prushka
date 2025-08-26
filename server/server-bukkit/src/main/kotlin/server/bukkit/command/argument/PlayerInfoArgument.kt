package server.bukkit.command.argument

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import server.bukkit.nms.message
import server.common.message.PlayerInfoMessage


/**
 * @author Nipuru
 * @since 2025/08/05 17:30
 */
@Suppress("UnstableApiUsage")
object PlayerInfoArgument : CustomArgumentType.Converted<PlayerInfoMessage, String> {

    private val ERROR_PLAYER_NOT_FOUND = DynamicCommandExceptionType { playerName ->
        "玩家不存在: '$playerName'".message()
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.string()
    }

    override fun convert(playerName: String): PlayerInfoMessage {
        throw ERROR_PLAYER_NOT_FOUND.create(playerName)
    }
}