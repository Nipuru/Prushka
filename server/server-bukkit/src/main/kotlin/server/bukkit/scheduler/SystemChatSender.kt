package server.bukkit.scheduler

import net.afyer.afybroker.client.Broker
import org.bukkit.scheduler.BukkitTask
import server.bukkit.util.ScheduleTask
import server.common.message.SystemChatMessage
import java.util.concurrent.ConcurrentLinkedQueue


/**
 * @author Nipuru
 * @since 2025/09/14 17:19
 */
object SystemChatSender : ScheduleTask(delay = 1L, period = 1L, async = true) {
    private val messageQueue = ConcurrentLinkedQueue<SystemChatMessage.Message>()

    fun send(message: SystemChatMessage.Message) {
        messageQueue += message
    }

    override fun run(task: BukkitTask) {
        if (messageQueue.isEmpty()) return
        val messages = mutableListOf<SystemChatMessage.Message>()
        while (messageQueue.isNotEmpty()) {
            val message = messageQueue.poll()
            messages += message
        }
        Broker.oneway(SystemChatMessage(messages))
    }
}

