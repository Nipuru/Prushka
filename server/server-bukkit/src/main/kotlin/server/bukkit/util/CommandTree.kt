package server.bukkit.util

import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
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
            commands.registrar().register(root, description)
            aliases.map { alias ->
                buildRedirect(alias, root)
            }.forEach { commands.registrar().register(it, description) }
        }
    }

    // 复制节点以解决重定向不生效的问题
    // https://github.com/Mojang/brigadier/issues/46
    fun buildRedirect(alias: String, destination: LiteralCommandNode<CommandSourceStack>): LiteralCommandNode<CommandSourceStack> {
        val builder = Commands.literal(alias.lowercase())
            .requires(destination.getRequirement())
            .forward(destination.getRedirect(), destination.getRedirectModifier(), destination.isFork)
            .executes(destination.getCommand())
        for (child in destination.getChildren()) {
            builder.then(child)
        }
        return builder.build()
    }
}