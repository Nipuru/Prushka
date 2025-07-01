package server.bukkit.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import server.bukkit.MessageType
import server.bukkit.logger.logger
import server.bukkit.route.Router
import server.bukkit.util.hasTag
import server.bukkit.util.submit
import server.common.message.shared.GetPlayerInfoRequest
import server.common.message.shared.PlayerInfoMessage


/**
 * @author Nipuru
 * @since 2024/11/20 10:29
 */
internal fun sync(sender: CommandSender, block: () -> Unit) {
    submit(async = false) {
        handleCommand(sender, block)
    }
}

internal fun async(sender: CommandSender, block: () -> Unit) {
    submit {
        handleCommand(sender, block)
    }
}

internal fun handleCommand(sender: CommandSender, block: () -> Unit) {
    try {
        block.invoke()
    } catch (e: CommandInterruptException) {
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
        throw CommandInterruptException
    }
}

internal fun toInt(sender: CommandSender, number: String, radix: Int = 10): Int {
    validateInputs(sender, number)
    try {
        return number.toInt(radix)
    } catch (e: NumberFormatException) {
        MessageType.WARNING.sendMessage(sender, "$number 不是有效的数字")
        throw CommandInterruptException
    }
}

internal fun getBukkitPlayer(sender: CommandSender, name: String): Player {
    validateInputs(sender, name)
    val bukkitPlayer = Bukkit.getPlayerExact(name)
    if (bukkitPlayer != null) return bukkitPlayer
    MessageType.WARNING.sendMessage(sender, "玩家 $name 不在线")
    throw CommandInterruptException
}

internal fun getPlayerInfo(sender: CommandSender, name: String): PlayerInfoMessage {
    if (Bukkit.isPrimaryThread()) throw IllegalStateException("primary thread")
    validateInputs(sender, name)
    val playerInfo = Router.sharedRequest<PlayerInfoMessage?>(GetPlayerInfoRequest(name))
    if (playerInfo != null) return playerInfo
    MessageType.WARNING.sendMessage(sender, "玩家 $name 不存在")
    throw CommandInterruptException
}

internal fun validateInputs(sender: CommandSender, vararg args: String) {
    for (arg in args) {
        if (!isValid(arg)) {
            MessageType.WARNING.sendMessage(sender, "非法的输入")
            throw CommandInterruptException
        }
    }
}

internal object CommandInterruptException : Exception() {
    private fun readResolve(): Any = CommandInterruptException
}

private fun isValid(arg: String): Boolean {
    return !hasTag(arg)
}