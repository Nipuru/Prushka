package top.nipuru.prushka.server.game.command

import top.nipuru.prushka.server.common.message.shared.PlayerInfoMessage
import top.nipuru.prushka.server.common.message.shared.GetPlayerInfoRequest
import top.nipuru.prushka.server.game.MessageType
import top.nipuru.prushka.server.game.logger.logger
import top.nipuru.prushka.server.game.route.sharedRequest
import top.nipuru.prushka.server.game.util.hasTag
import top.nipuru.prushka.server.game.util.submit
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


/**
 * @author Nipuru
 * @since 2024/11/20 10:29
 */
internal fun sync(sender: CommandSender, block: () -> Unit) {
    submit(async = false) {
        top.nipuru.prushka.server.game.command.handleCommand(sender, block)
    }
}

internal fun async(sender: CommandSender, block: () -> Unit) {
    submit {
        top.nipuru.prushka.server.game.command.handleCommand(sender, block)
    }
}

internal fun handleCommand(sender: CommandSender, block: () -> Unit) {
    try {
        block.invoke()
    } catch (e: top.nipuru.prushka.server.game.command.CommandInterruptException) {
        // ignore
    } catch (e: Exception) {
        logger.error(e.message, e)
        MessageType.WARNING.sendMessage(sender, "命令产生了一个错误")
    }
}

internal inline fun sendUsageIf(sender: CommandSender, vararg usage: String, func: () -> Boolean) {
    if (func.invoke()) {
        if (usage.size == 1) {
            MessageType.WARNING.sendMessage(sender, "用法: <white>${usage[0]}</white>")
        } else {
            MessageType.WARNING.sendMessage(sender, "用法: <white>${usage.joinToString(prefix = "<br/>- ")}</white>")
        }
        throw top.nipuru.prushka.server.game.command.CommandInterruptException
    }
}

internal fun toInt(sender: CommandSender, number: String, radix: Int = 10): Int {
    top.nipuru.prushka.server.game.command.validateInputs(sender, number)
    try {
        return number.toInt(radix)
    } catch (e: NumberFormatException) {
        MessageType.WARNING.sendMessage(sender, "$number 不是有效的数字")
        throw top.nipuru.prushka.server.game.command.CommandInterruptException
    }
}

internal fun getBukkitPlayer(sender: CommandSender, name: String): Player {
    top.nipuru.prushka.server.game.command.validateInputs(sender, name)
    val bukkitPlayer = Bukkit.getPlayerExact(name)
    if (bukkitPlayer != null) return bukkitPlayer
    MessageType.WARNING.sendMessage(sender, "玩家 $name 不在线")
    throw top.nipuru.prushka.server.game.command.CommandInterruptException
}

internal fun getPlayerInfo(sender: CommandSender, name: String): PlayerInfoMessage {
    if (Bukkit.isPrimaryThread()) throw IllegalStateException("primary thread")
    top.nipuru.prushka.server.game.command.validateInputs(sender, name)
    val playerInfo = sharedRequest<PlayerInfoMessage?>(GetPlayerInfoRequest(name))
    if (playerInfo != null) return playerInfo
    MessageType.WARNING.sendMessage(sender, "玩家 $name 不存在")
    throw top.nipuru.prushka.server.game.command.CommandInterruptException
}

internal fun validateInputs(sender: CommandSender, vararg args: String) {
    for (arg in args) {
        if (!top.nipuru.prushka.server.game.command.isValid(arg)) {
            MessageType.WARNING.sendMessage(sender, "非法的输入")
            throw top.nipuru.prushka.server.game.command.CommandInterruptException
        }
    }
}

internal object CommandInterruptException : Exception() {
    private fun readResolve(): Any = top.nipuru.prushka.server.game.command.CommandInterruptException
}

private fun isValid(arg: String): Boolean {
    return !hasTag(arg)
}