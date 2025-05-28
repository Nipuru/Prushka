package top.nipuru.prushka.game.gameplay.chat

import net.afyer.afybroker.client.Broker
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextColor.color
import top.nipuru.prushka.common.message.FragmentMessage
import top.nipuru.prushka.common.message.PlayerChatMessage
import top.nipuru.prushka.common.message.PlayerPrivateChatMessage
import top.nipuru.prushka.common.message.database.PlayerDataRequestMessage
import top.nipuru.prushka.common.message.shared.PlayerInfoMessage
import top.nipuru.prushka.common.sheet.stRankMap
import top.nipuru.prushka.game.MessageType
import top.nipuru.prushka.game.gameplay.player.BaseManager
import top.nipuru.prushka.game.gameplay.player.DataInfo
import top.nipuru.prushka.game.gameplay.player.GamePlayer
import top.nipuru.prushka.game.gameplay.player.preload
import top.nipuru.prushka.game.time.TimeManager
import top.nipuru.prushka.game.util.submit
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ChatManager(player: GamePlayer) : BaseManager(player) {

    private lateinit var data: ChatData

    fun preload(request: PlayerDataRequestMessage) {
        request.preload(ChatData::class.java)
    }

    fun unpack(dataInfo: DataInfo) {
        data = dataInfo.unpack(ChatData::class.java) ?: ChatData().also { player.insert(it) }
    }

    fun pack(dataInfo: DataInfo) {
        dataInfo.pack(data)
    }

    var msgTarget: String
        get() = data.msgTarget
        set(msgTarget) {
            data.msgTarget = msgTarget
        }

    fun hasMsgTarget(): Boolean {
        return data.msgTarget.isNotEmpty()
    }

    fun clearMsgTarget() {
        data.msgTarget = ""
    }

    var mute: Long
        get() = data.mute
        set(time) {
            data.mute = time
            player.update(data, ChatData::mute)
        }

    val isMuted: Boolean
        get() = data.mute > TimeManager.now

    fun unmute() {
        val now: Long = TimeManager.now
        if (data.mute == now) return
        data.mute = now
        player.update(data, ChatData::mute)
    }

    val muteDateTime: LocalDateTime
        get() {
            val dateTime = Instant.ofEpochMilli(data.mute)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            return dateTime
        }

    fun couldReceivePrivate(sender: PlayerInfoMessage): Boolean {
        return true
    }

    fun receivePublic(sender: PlayerInfoMessage, fragments: Array<FragmentMessage>) {
        val rank = stRankMap[sender.rankId]!!
        val builder = text()
        builder.color(TextColor.fromHexString(rank.chatColor))
        builder.append(publicChatPrefix(sender))
        builder.append(MessageFormat.format(sender, player, fragments))
        player.bukkitPlayer.sendMessage(builder.build())
    }

    fun receivePrivate(sender: PlayerInfoMessage, receiver: String, fragments: Array<FragmentMessage>) {
        val builder = text()
        if (sender.playerId == player.playerId) {
            builder.append(privateSenderPrefix(receiver))
            builder.color(color(0x93a5ad))
            builder.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg $receiver "))
        } else {
            builder.append(privateReceiverPrefix(sender.name))
            builder.color(color(0xc4eafa))
            builder.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg ${sender.name} "))
        }

        builder.append(MessageFormat.format(sender, player, fragments))
        player.bukkitPlayer.sendMessage(builder.build())
    }

    fun sendPublic(message: String) {
        val fragments = MessageFormat.parse(player, message)
        val request = PlayerChatMessage(player.core.playerInfo, fragments)

        submit {
            val result = Broker.invokeSync<Int>(request)
            when (result) {
                PlayerChatMessage.SUCCESS -> {
                    // 由广播显示自己发送消息
                }

                PlayerChatMessage.FAILURE -> MessageType.FAILED.sendMessage(player.bukkitPlayer, "消息发送失败。")
                PlayerChatMessage.RATE_LIMIT -> MessageType.FAILED.sendMessage(
                    player.bukkitPlayer,
                    "你的发言频率过快，请稍候再试。"
                )
            }
        }
    }

    fun sendPrivate(receiver: String, message: String) {
        val fragments: Array<FragmentMessage> = MessageFormat.parse(player, message)
        val request = PlayerPrivateChatMessage(player.core.playerInfo, fragments, receiver)

        submit {
            val result = Broker.invokeSync<Int>(request)
            when (result) {
                PlayerPrivateChatMessage.SUCCESS -> {
                    // 给自己显示一条私聊消息
                    receivePrivate(player.core.playerInfo, receiver, fragments)
                }

                PlayerPrivateChatMessage.FAILURE -> MessageType.FAILED.sendMessage(player.bukkitPlayer, "消息发送失败。")
                PlayerPrivateChatMessage.RATE_LIMIT -> MessageType.FAILED.sendMessage(
                    player.bukkitPlayer,
                    "你的发言频率过快，请稍候再试。"
                )

                PlayerPrivateChatMessage.NOT_ONLINE -> MessageType.FAILED.sendMessage(
                    player.bukkitPlayer,
                    "私聊玩家 ",
                    receiver,
                    " 不在线"
                )

                PlayerPrivateChatMessage.DENY -> MessageType.FAILED.sendMessage(
                    player.bukkitPlayer,
                    "私聊玩家 ",
                    receiver,
                    " 屏蔽了聊天消息"
                )
            }
        }
    }

    private fun publicChatPrefix(sender: PlayerInfoMessage): Component {
        val rank = stRankMap[sender.rankId]!!
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
            text(receiver)
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
            text(sender)
                .color(color(0x95cee9))
        )

        builder.append(text(": ").color(color(0xd3d3d3)))
        return builder.build()
    }
}
