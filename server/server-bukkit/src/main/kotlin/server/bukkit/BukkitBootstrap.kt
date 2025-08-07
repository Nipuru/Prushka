package server.bukkit

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.keys.DamageTypeKeys
import io.papermc.paper.registry.keys.tags.DamageTypeTagKeys
import net.kyori.adventure.key.Key
import server.bukkit.command.AfkCommand
import server.bukkit.command.FriendCommand
import server.bukkit.command.PrushkaCommand
import server.bukkit.command.WhereAmICommand


/**
 * paper 启动类
 * 用于注册一些原版特性支持，例如 Registry.
 *
 * @author Nipuru
 * @since 2025/08/05 15:37
 */
@Suppress("UnstableApiUsage")
class BukkitBootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        val manager = context.lifecycleManager
    }
}