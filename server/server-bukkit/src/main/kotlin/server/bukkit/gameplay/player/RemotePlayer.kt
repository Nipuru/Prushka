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
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import server.bukkit.scheduler.AudienceMessenger.send
import server.bukkit.util.text.string
import server.common.message.AudienceMessage.Message.*
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
        send(SystemChat(receiver = playerInfo.uniqueId, message = message.string()))
    }

    override fun deleteMessage(signature: SignedMessage.Signature) =
        throw UnsupportedOperationException("Not supported")

    override fun sendActionBar(message: Component) {
        send(ActionBar(receiver = playerInfo.uniqueId, message = message.string()))
    }

    override fun sendPlayerListHeader(header: Component) {
        send(PlayerListHeader(receiver = playerInfo.uniqueId, header = header.string()))
    }

    override fun sendPlayerListFooter(footer: Component) {
        send(PlayerListFooter(receiver = playerInfo.uniqueId, footer = footer.string()))
    }

    override fun sendPlayerListHeaderAndFooter(header: Component, footer: Component) {
        send(PlayerListHeaderAndFooter(receiver = playerInfo.uniqueId, header = header.string(), footer = footer.string()))
    }

    override fun <T> sendTitlePart(part: TitlePart<T>, value: T) {
        val request = when (part) {
            TitlePart.TITLE -> {
                TitlePartTitle(
                    receiver = playerInfo.uniqueId,
                    title = (value as Component).string()
                )
            }
            TitlePart.SUBTITLE -> {
                TitlePartSubtitle(
                    receiver = playerInfo.uniqueId,
                    subtitle = (value as Component).string()
                )
            }
            TitlePart.TIMES -> {
                val times = value as Title.Times
                TitlePartTimes(
                    receiver = playerInfo.uniqueId,
                    fadeIn = times.fadeIn().toMillis(),
                    stay = times.stay().toMillis(),
                    fadeOut = times.fadeOut().toMillis()
                )
            }
            else -> error("Unsupported title part")
        }
        send(request)
    }
    override fun clearTitle() {
        send(TitleClear(receiver = playerInfo.uniqueId))
    }

    override fun resetTitle() {
        send(TitleReset(receiver = playerInfo.uniqueId))
    }

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
    override fun openBook(book: Book) {
        send(Book(receiver = playerInfo.uniqueId, title = book.title().string(), author = book.author().string(), pages = book.pages().map { it.string() }))
    }

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