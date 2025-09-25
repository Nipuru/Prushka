package server.bukkit.processor

import com.alipay.remoting.AsyncContext
import com.alipay.remoting.BizContext
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.sound.SoundStop
import net.kyori.adventure.title.Title.Times
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import server.bukkit.BukkitPlugin
import server.bukkit.util.schedule
import server.bukkit.util.text.component
import server.common.message.AudienceMessage
import server.common.message.AudienceMessage.Message.SystemChat
import java.time.Duration.ofMillis


/**
 * @author Nipuru
 * @since 2025/09/14 17:05
 */
class AudienceBukkitProcessor : AsyncUserProcessor<AudienceMessage>() {

    override fun handleRequest(bizContext: BizContext, asyncContext: AsyncContext, request: AudienceMessage) {
        BukkitPlugin.schedule {
            request.messages.forEach { message ->
                Bukkit.getPlayer(message.receiver)?.send(message)
            }
        }
    }

    private fun Player.send(message: AudienceMessage.Message) = when(message) {
        is SystemChat ->
            sendMessage(message.message.component())
        is AudienceMessage.Message.ActionBar ->
            sendActionBar(message.message.component())
        is AudienceMessage.Message.PlayerListFooter ->
            sendPlayerListFooter(message.footer.component())
        is AudienceMessage.Message.PlayerListHeader ->
            sendPlayerListHeader(message.header.component())
        is AudienceMessage.Message.PlayerListHeaderAndFooter ->
            sendPlayerListHeaderAndFooter(message.header.component(), message.footer.component())
        is AudienceMessage.Message.TitlePartTitle ->
            sendTitlePart(TitlePart.TITLE, message.title.component())
        is AudienceMessage.Message.TitlePartSubtitle ->
            sendTitlePart(TitlePart.SUBTITLE, message.subtitle.component())
        is AudienceMessage.Message.TitlePartTimes ->
            sendTitlePart(TitlePart.TIMES, Times.times(ofMillis(message.fadeIn), ofMillis(message.stay), ofMillis(message.fadeOut)))
        is AudienceMessage.Message.TitleClear ->
            clearTitle()
        is AudienceMessage.Message.TitleReset ->
            resetTitle()
        is AudienceMessage.Message.Book ->
            openBook(Book.builder().title(message.title.component()).author(message.author.component()).pages(message.pages.map { it.component() }))
        is AudienceMessage.Message.PlaySound -> {
            val sound = Sound.sound().apply {
                type(Key.key(message.name))
                source(Source.entries[message.source])
                volume(message.volume)
                pitch(message.pitch)
                message.seed?.let { seed(it) }
            }
            playSound(sound.build())
        }
        is AudienceMessage.Message.StopSound -> {
            val name = message.name?.let { Key.key(it) }
            val source = message.source?.let { Source.entries[it] }
            val stop = when {
                name != null && source != null -> SoundStop.namedOnSource(name, source)
                name != null -> SoundStop.named(name)
                source != null -> SoundStop.source(source)
                else -> SoundStop.all()
            }
            stopSound(stop)
        }
    }

    override fun interest(): String {
        return AudienceMessage::class.java.name
    }

}