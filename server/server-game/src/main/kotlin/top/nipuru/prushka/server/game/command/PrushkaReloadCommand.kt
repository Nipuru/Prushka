package top.nipuru.prushka.server.game.command

import top.nipuru.prushka.server.common.sheet.Sheet
import top.nipuru.prushka.server.game.plugin
import org.bukkit.command.CommandSender
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
        Sheet.load(File(plugin.dataFolder, "jsons").absolutePath)
    }
}