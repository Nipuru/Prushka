package top.nipuru.minegame.game.command

import top.nipuru.minegame.game.MessageType
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

/**
 * @author Nipuru
 * @since 2022/7/12 15:58
 */
abstract class AbstractCommand protected constructor(name: String, vararg alias: String) :
    Command(name, "", "", listOf(*alias)) {

    private val subCommands: MutableList<AbstractCommand> = ArrayList()

    protected fun subCommand(subCommand: AbstractCommand): AbstractCommand {
        subCommands.add(subCommand)
        return this
    }

    private fun matchCommand(command: String): Boolean {
        return label.equals(command, ignoreCase = true) || aliases.stream()
            .anyMatch { anotherString -> command.equals(anotherString, ignoreCase = true) }
    }

    final override fun execute(sender: CommandSender, command: String, args: Array<String>): Boolean {
        if (!hasPermission(sender)) {
            MessageType.WARNING.sendMessage(sender, "你没有权限此命令")
            return true
        }
        if (!canConsoleExecute() && sender !is Player) {
            MessageType.WARNING.sendMessage(sender, "只有玩家才能执行此命令")
            return true
        }
        if (args.isNotEmpty()) {
            for (executor in subCommands) {
                if (executor.matchCommand(args[0])) {
                    return executor.execute(sender, command, Arrays.copyOfRange(args, 1, args.size))
                }
            }
        }
        handleCommand(sender) { onCommand(sender, args) }
        return true
    }

    final override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>): List<String> {
        if (!hasPermission(sender)) {
            return emptyList()
        }
        if (args.isNotEmpty()) {
            for (executor in subCommands) {
                if (executor.matchCommand(args[0])) {
                    return executor.tabComplete(sender, alias, Arrays.copyOfRange(args, 1, args.size))
                }
            }
        }
        return onTabComplete(sender, args)
    }

    final override fun testPermissionSilent(target: CommandSender): Boolean {
        return hasPermission(target)
    }

    final override fun testPermission(target: CommandSender): Boolean {
        return testPermissionSilent(target)
    }

    fun register(plugin: Plugin) {
        put("${plugin.name}:$label")
        put(label)
        for (alias in aliases) {
            put("${plugin.name}:$alias")
            put(alias)
        }
    }

    private fun put(label: String) {
        Bukkit.getServer().commandMap.knownCommands[label] = this
    }

    open fun hasPermission(sender: CommandSender) = true

    open fun canConsoleExecute() = true

    open fun onCommand(sender: CommandSender, args: Array<String>) {}

    open fun onTabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return if (args.size == 1) {
            subCommands.map { it.label }.filter { it.startsWith(args[0], ignoreCase = true) }
        } else {
            emptyList()
        }
    }
}


