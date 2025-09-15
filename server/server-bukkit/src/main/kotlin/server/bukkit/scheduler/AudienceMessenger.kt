package server.bukkit.scheduler

import net.afyer.afybroker.client.Broker
import org.bukkit.scheduler.BukkitTask
import server.bukkit.util.ScheduleTask
import server.common.message.AudienceMessage
import server.common.message.AudienceMessage.Message
import java.util.concurrent.ConcurrentLinkedQueue


/**
 * @author Nipuru
 * @since 2025/09/14 17:19
 */
object AudienceMessenger : ScheduleTask(delay = 1L, period = 1L, async = true) {
    private val messageQueue = ConcurrentLinkedQueue<Message>()

    fun send(message: Message) {
        messageQueue += message
    }

    override fun run(task: BukkitTask) {
        if (messageQueue.isEmpty()) return
        val messages = mutableListOf<Message>()
        while (messageQueue.isNotEmpty()) {
            val message = messageQueue.poll()
            messages += message
        }
        Broker.oneway(AudienceMessage(messages))
    }
}

