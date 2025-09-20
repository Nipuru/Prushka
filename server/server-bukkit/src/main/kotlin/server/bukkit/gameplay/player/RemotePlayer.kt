package server.bukkit.gameplay.player

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.dialog.DialogLike
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.resource.ResourcePackRequest
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.SoundStop
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.TitlePart
import server.bukkit.scheduler.AudienceMessenger.send
import server.bukkit.util.text.TextFactory
import server.common.message.AudienceMessage.Message.SystemChat
import server.common.message.PlayerInfoMessage
import java.util.*


/**
 * 代表集群上的一个玩家 可能在线也可能不在线。
 * 用于向当前玩家发送消息。
 *
 * @param playerInfo 玩家信息。
 */
val PlayerInfoMessage.remotePlayer: RemotePlayer get() = RemotePlayer(this)

class RemotePlayer(val playerInfo: PlayerInfoMessage) : Audience {
    override fun sendMessage(message: Component) {
        val serialized = TextFactory.instance.miniMessage.serialize(message)
        val request = SystemChat(receiver = playerInfo.uniqueId, message = serialized)
        send(request)
    }

    override fun deleteMessage(signature: SignedMessage.Signature) =
        throw UnsupportedOperationException("Not supported")
    override fun sendActionBar(message: Component) =
        throw UnsupportedOperationException("Not supported")
    override fun sendPlayerListHeaderAndFooter(header: Component, footer: Component) =
        throw UnsupportedOperationException("Not supported")
    override fun <T> sendTitlePart(part: TitlePart<T>, value: T) =
        throw UnsupportedOperationException("Not supported")
    override fun clearTitle() =
        throw UnsupportedOperationException("Not supported")
    override fun resetTitle() =
        throw UnsupportedOperationException("Not supported")
    override fun showBossBar(bar: BossBar) =
        throw UnsupportedOperationException("Not supported")
    override fun hideBossBar(bar: BossBar) =
        throw UnsupportedOperationException("Not supported")
    override fun playSound(sound: Sound) =
        throw UnsupportedOperationException("Not supported")
    override fun playSound(sound: Sound, x: Double, y: Double, z: Double) =
        throw UnsupportedOperationException("Not supported")
    override fun playSound(sound: Sound, emitter: Sound.Emitter) =
        throw UnsupportedOperationException("Not supported")
    override fun stopSound(stop: SoundStop) =
        throw UnsupportedOperationException("Not supported")
    override fun openBook(book: Book) =
        throw UnsupportedOperationException("Not supported")
    override fun sendResourcePacks(request: ResourcePackRequest) =
        throw UnsupportedOperationException("Not supported")
    override fun removeResourcePacks(id: UUID, vararg others: UUID) =
        throw UnsupportedOperationException("Not supported")
    override fun clearResourcePacks() =
        throw UnsupportedOperationException("Not supported")
    override fun showDialog(dialog: DialogLike) =
        throw UnsupportedOperationException("Not supported")
    override fun closeDialog() =
        throw UnsupportedOperationException("Not supported")
}