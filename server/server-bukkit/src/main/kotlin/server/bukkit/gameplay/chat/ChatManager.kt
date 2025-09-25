package server.bukkit.gameplay.chat

import net.afyer.afybroker.client.Broker
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextColor.color
import server.bukkit.BukkitPlugin
import server.bukkit.gameplay.player.*
import server.bukkit.time.TimeManager
import server.bukkit.util.completeFuture
import server.common.message.FragmentMessage
import server.common.message.PlayerChatMessage
import server.common.message.PlayerInfoMessage
import server.common.message.PlayerPrivateChatMessage
import server.common.sheet.Sheet
import server.common.sheet.getStRank
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.CompletableFuture
import kotlin.math.max

class ChatManager(player: GamePlayer) : BaseManager(player) {

    private lateinit var data: ChatData
    private var lastChatTime = System.currentTimeMillis()
    private val chatPerSecond = 0.25
    private val chatCapacity = 3

    fun preload(request: TableInfos) {
        request.preload<ChatData>()
    }

    fun unpack(dataInfo: DataInfo) {
        data = dataInfo.unpack<ChatData>() ?: player.insert(ChatData())
    }

    fun pack(dataInfo: DataInfo) {
        dataInfo.pack(data)
    }

    var mute: Long
        get() = data.mute
        set(time) {
            data.mute = time
            player.update(data, ChatData::mute)
        }

    val isMuted: Boolean get() = data.mute > TimeManager.now

    val muteDateTime: LocalDateTime get() {
        val dateTime = Instant.ofEpochMilli(data.mute)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        return dateTime
    }

    var msgTarget: String
        get() = data.msgTarget
        set(value) {
            data.msgTarget = value
            player.update(data, ChatData::msgTarget)
        }

    fun unmute() {
        val now: Long = TimeManager.now
        if (data.mute == now) return
        data.mute = now
        player.update(data, ChatData::mute)
    }

    fun receiveChat(sender: PlayerInfoMessage, fragments: Array<FragmentMessage>) {
        val rank = Sheet.getStRank(sender.rankId, player.locale)!!
        val builder = text()
        builder.color(TextColor.fromHexString(rank.chatColor))
        builder.append(chatPrefix(sender))
        builder.append(MessageFormat.format(sender, player, fragments))
        player.bukkitPlayer.sendMessage(builder.build())
    }

    fun receivePrivateChat(sender: PlayerInfoMessage, receiver: String, fragments: Array<FragmentMessage>, isSender: Boolean) {
        val builder = text()
        if (isSender) {
            builder.append(privateSenderPrefix(receiver))
            builder.color(color(0x93a5ad))
            builder.clickEvent(ClickEvent.suggestCommand("/msg $receiver "))
        } else {
            builder.append(privateReceiverPrefix(sender.name))
            builder.color(color(0xc4eafa))
            builder.clickEvent(ClickEvent.suggestCommand("/msg ${sender.name} "))
        }

        builder.append(MessageFormat.format(sender, player, fragments))
        player.bukkitPlayer.sendMessage(builder.build())
    }

    fun rateLimit(): Boolean {
        if (data.rateLimit == 0.0) {
            lastChatTime = System.currentTimeMillis()
            data.rateLimit += 1.0
            return false
        }
        // 执行漏水
        val waterLeaked = ((System.currentTimeMillis() - lastChatTime) * chatPerSecond / 1000)
        val waterLeft = data.rateLimit - waterLeaked
        data.rateLimit = max(0.0, waterLeft)
        lastChatTime = System.currentTimeMillis()
        if (data.rateLimit < chatCapacity) {
            data.rateLimit += 1.0
            return false
        } else {
            return true
        }
    }

    /**
     * 发送公屏消息
     */
    fun sendChat(message: String): CompletableFuture<Boolean> {
        val fragments = MessageFormat.parse(player, message)
        val request = PlayerChatMessage(player.core.playerInfo, fragments)

        return BukkitPlugin.bizThread.completeFuture {
            Broker.invokeSync(request)
        }
    }

    /**
     * 发送私聊消息
     */
    fun sendPrivateChat(receiver: String, message: String): CompletableFuture<Boolean> {
        val fragments: Array<FragmentMessage> = MessageFormat.parse(player, message)
        val request = PlayerPrivateChatMessage(
            sender = player.core.playerInfo,
            fragments = fragments,
            receiver = receiver
        )

        return BukkitPlugin.bizThread.completeFuture {
            Broker.invokeSync(request)
        }
    }

    private fun chatPrefix(sender: PlayerInfoMessage): Component {
        val rank = Sheet.getStRank(sender.rankId, player.locale)!!
        val builder = text()

        // 玩家名字
        builder.append(
            text(sender.name)
                .color(TextColor.fromHexString(rank.nameColor))
        )

        builder.append(text(":").color(NamedTextColor.GRAY))
        builder.append(Component.space())

        return builder.build()
    }

    private fun privateSenderPrefix(receiver: String): Component {
        val builder = text()
        builder.color(NamedTextColor.WHITE)

        // 接受者名字
        builder.append(
            text("你对 $receiver 说")
                .color(color(0x98b7c5))
        )

        builder.append(text(": ").color(color(0xd3d3d3)))
        return builder.build()
    }

    private fun privateReceiverPrefix(sender: String): Component {
        val builder = text()
        builder.color(NamedTextColor.WHITE)

        // 发送名字
        builder.append(
            text("$sender 对你说")
                .color(color(0x95cee9))
        )

        builder.append(text(": ").color(color(0xd3d3d3)))
        return builder.build()
    }
}
