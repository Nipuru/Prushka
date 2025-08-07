@file:Suppress("UnstableApiUsage")
package server.bukkit.command

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.gameplay.player.GamePlayers
import server.bukkit.nms.message


/**
 * @author Nipuru
 * @since 2025/08/05 17:41
 */
val ERROR_NOT_PLAYER = SimpleCommandExceptionType("只有玩家才能执行此命令".message())

fun <T : ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.requireOperator(): T = requires { it.sender.isOp }

fun <T : ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack, T>.requirePlayer(): T = requires { it.sender is Player }

inline fun <reified T> CommandContext<*>.getArgument(name: String): T = getArgument(name, T::class.java)

val CommandSourceStack.gamePlayer: GamePlayer
    get() {
        val entity = this.executor
        if (entity !is Player) throw ERROR_NOT_PLAYER.create()
        return GamePlayers.getPlayer(entity.uniqueId)
    }