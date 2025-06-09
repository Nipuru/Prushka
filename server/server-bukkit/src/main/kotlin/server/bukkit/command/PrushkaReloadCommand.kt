package server.bukkit.command

import org.bukkit.command.CommandSender
import server.bukkit.plugin
import java.io.File


/**
 * 重载插件配置
 * Cmd: /prushka reload
 *
 * @author Nipuru
 * @since 2024/11/29 09:35
 */
class PrushkaReloadCommand : AbstractCommand("reload") {
    override fun onCommand(sender: CommandSender, args: Array<String>) {
        // 重载表格
        server.common.sheet.Sheet.load(File(plugin.dataFolder, "jsons").absolutePath)
    }
}