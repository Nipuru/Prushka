package server.bukkit

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin


/**
 * paper 启动类
 * 用于注册一些原版特性支持，例如 Registry.
 * 以及自定义插件实例化 支持 kotlin object
 *
 * @author Nipuru
 * @since 2025/08/05 15:37
 */
@Suppress("UnstableApiUsage")
class BukkitBootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        val manager = context.lifecycleManager
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return BukkitPlugin
    }
}