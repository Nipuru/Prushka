package server.bukkit.nms

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.protocol.Packet
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player


class PacketListener(player: Player) : ChannelDuplexHandler() {

    private val serverPlayer = (player as CraftPlayer).handle

    private fun cancelPacket(packet: Packet<*>): Boolean {
        return false
    }

    override fun channelRead(ctx: ChannelHandlerContext, packet: Any) {
        if (packet is Packet<*> && cancelPacket(packet)) return
        super.channelRead(ctx, packet)
    }

    override fun write(ctx: ChannelHandlerContext, packet: Any, promise: ChannelPromise) {
        if (packet is Packet<*> && cancelPacket(packet)) return
        super.write(ctx, packet, promise)
    }


}
