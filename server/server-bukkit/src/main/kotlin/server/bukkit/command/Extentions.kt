package server.bukkit.command

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.gameplay.player.GamePlayers


/**
 * @author Nipuru
 * @since 2025/08/05 17:41
 */
internal val PLAYER_ONLY = SimpleCommandExceptionType(LiteralMessage("只有玩家才能执行此命令"))

internal val CommandSender.player: GamePlayer get() = if (this is Player) GamePlayers.getPlayer(uniqueId) else throw PLAYER_ONLY.create()