package server.bukkit.util

import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.Plugin


/**
 * 命令树
 * @author Nipuru
 * @since 2025/09/25 10:48
 */
interface CommandTree {
    val root: LiteralCommandNode<CommandSourceStack>
    val description: String? get() = null
    val aliases: Collection<String> get() = emptyList()

    fun register(plugin: Plugin) {
        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            commands.registrar().register(root, description, aliases)
        }
    }
}