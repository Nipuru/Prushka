package server.bukkit.command

import com.google.gson.JsonObject
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.Commands.argument
import io.papermc.paper.command.brigadier.Commands.literal
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.World
import server.bukkit.MessageType
import server.bukkit.command.argument.PlayerInfoArgument
import server.bukkit.config.Config
import server.bukkit.plugin
import server.bukkit.util.component
import server.bukkit.util.gson
import server.bukkit.util.submit
import server.common.message.PlayerInfoMessage
import server.common.message.TeleportType
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture


/**
 * 管理员命令根节点
 * Cmd: /prushka
 *
 * @author Nipuru
 * @since 2024/11/19 15:11
 */
@Suppress("UnstableApiUsage")
object PrushkaCommand {
    fun register(registrar: Commands) {
        registrar.register(literal("prushka")
            .requireOperator()
            .then(literal("text")
                .then(argument("args", StringArgumentType.greedyString())
                    .executes(::text)))
            .then(literal("reload")
                .executes(::reload))
            .then(literal("world")
                .then(argument("world_name", ArgumentTypes.world())
                    .executes(::world)))
            .then(literal("tpa")
                .then(argument("player_name", PlayerInfoArgument)
                    .executes(::tpa)))
            .then(literal("tpahere")
                .then(argument("player_name", PlayerInfoArgument)
                    .executes(::tpahere)))
            .then(literal("resourcepack")
                .executes(::resourcepack))
            .build())
    }

    /**
     * 发送文本消息
     * /prushka text <文本>
     */
    private fun text(context: CommandContext<CommandSourceStack>): Int {
        val sender = context.source.sender
        val message = StringArgumentType.getString(context, "args").component()
        sender.sendMessage(message)
        return Command.SINGLE_SUCCESS
    }

    /**
     * 重载配置文件
     * /prushka reload
     */
    private fun reload(context: CommandContext<CommandSourceStack>): Int {
        val sender = context.source.sender
        MessageType.INFO.sendMessage(sender, "正在重载配置文件...")
        plugin.reload()
        MessageType.INFO.sendMessage(sender, "重载完成")
        return Command.SINGLE_SUCCESS
    }

    /**
     * 世界传送
     * /prushka world <世界名>
     */
    private fun world(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        val world = context.getArgument<World>("world_name")
        if (player.bukkitPlayer.world == world) {
            MessageType.FAILED.sendMessage(player, "你已经位于世界 ${world.name}")
            return Command.SINGLE_SUCCESS
        }
        player.bukkitPlayer.teleport(world.spawnLocation)
        return Command.SINGLE_SUCCESS
    }

    /**
     * 玩家tpa
     * /prushka tpa <玩家名>
     */
    fun tpa(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        val target = context.getArgument<PlayerInfoMessage>("player_name")
        player.teleport.teleport(target.name, TeleportType.TPA)
        return Command.SINGLE_SUCCESS
    }

    /**
     * 玩家tphere
     * /prushka tpahere <玩家名>
     */
    fun tpahere(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        val target = context.getArgument<PlayerInfoMessage>("player_name")
        player.teleport.teleport(target.name, TeleportType.TPAHERE)
        return Command.SINGLE_SUCCESS
    }

    /**
     * 下载资源包
     * /prushka resourcepack
     */
    fun resourcepack(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.gamePlayer
        val url = Config.RESOURCEPACK_URL.string()

        // 异步获取资源包信息
        CompletableFuture.runAsync {
            try {
                var uri = URI.create(url)
                val client = HttpClient.newHttpClient()
                val request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                client.close()

                if (response.statusCode() == 200) {
                    val jsonResponse = gson.fromJson(response.body(), JsonObject::class.java)

                    if (jsonResponse.get("success").asBoolean) {
                        val data = jsonResponse.getAsJsonObject("data")
                        val downloadUrl = data.get("download_url").asString
                        val hash = data.get("hash").asString

                        // 构建完整的下载URL
                        val baseUrl = "${uri.scheme}://${uri.host}:${uri.port}"
                        val fullDownloadUrl = baseUrl + downloadUrl

                        // 在主线程中发送资源包
                        submit(async = false) {
                            player.bukkitPlayer.setResourcePack(fullDownloadUrl, hash)
                            MessageType.INFO.sendMessage(context.source.sender, "资源包已发送给玩家 ${player.name}")
                        }
                    } else {
                        MessageType.WARNING.sendMessage(context.source.sender, "获取资源包信息失败")
                    }
                } else {
                    MessageType.WARNING.sendMessage(context.source.sender, "HTTP请求失败，状态码: ${response.statusCode()}")
                }
            } catch (e: Exception) {
                MessageType.WARNING.sendMessage(context.source.sender, "获取资源包信息时发生错误: ${e.message}")
            }
        }

        MessageType.INFO.sendMessage(context.source.sender, "正在获取资源包信息...")
        return Command.SINGLE_SUCCESS
    }
}



