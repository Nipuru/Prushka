package server.bukkit.nms

import com.mojang.brigadier.Message
import com.mojang.brigadier.exceptions.CommandSyntaxException
import io.netty.channel.ChannelHandler
import io.papermc.paper.adventure.PaperAdventure
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.minecraft.network.Connection
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import org.bukkit.Server
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftContainer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import server.bukkit.util.text.component
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

fun Player.hasDisconnected(): Boolean {
    val serverPlayer = (this as CraftPlayer).handle
    return serverPlayer.hasDisconnected()
}

fun Player.placeBackInInventory(itemStack: ItemStack) {
    val serverPlayer = (this as CraftPlayer).handle
    serverPlayer.inventory.placeItemBackInInventory(CraftItemStack.asNMSCopy(itemStack))
}

fun Player.quit() {
    val serverPlayer = (this as CraftPlayer).handle
    serverPlayer.connection.processedDisconnect = true
    val playerList = MinecraftServer.getServer().playerList
    playerList.remove(serverPlayer)
}

fun Collection<PotionEffect>.serialize(): ByteArray {
    val baos = ByteArrayOutputStream()
    val gzip = GZIPOutputStream(baos)
    val bukkitObjectOutputStream = BukkitObjectOutputStream(gzip)
    val array = this.toTypedArray()
    bukkitObjectOutputStream.writeInt(array.size)

    for (i in array.indices) {
        bukkitObjectOutputStream.writeObject(array[i])
    }

    bukkitObjectOutputStream.close()
    return baos.toByteArray()
}

fun ByteArray.deserializePotionEffects(): Collection<PotionEffect> {
    val bais = ByteArrayInputStream(this)
    val gzip = GZIPInputStream(bais)
    val bukkitObjectInputStream = BukkitObjectInputStream(gzip)

    val size = bukkitObjectInputStream.readInt()
    val effects = mutableListOf<PotionEffect>()

    for (i in 0 until size) {
        effects.add(bukkitObjectInputStream.readObject() as PotionEffect)
    }

    bukkitObjectInputStream.close()
    return effects
}

/** 将文本转换成 brigadier Message **/
fun String.message(): Message = PaperAdventure.asVanilla(component())

fun Entity.sendSpawn(players: List<Player>) {
    this as CraftEntity
    val serverEntity = ServerEntity(handle.level() as ServerLevel, handle, 0, false, { packet -> }, { packet, list -> }, emptySet())
    val packet = handle.getAddEntityPacket(serverEntity)
    for (player in players) {
        (player as CraftPlayer).handle.connection.send(packet)
    }
}

fun Entity.sendMetadata(players: List<Player>) {
    this as CraftEntity
    val packet = ClientboundSetEntityDataPacket(handle.id, handle.entityData.packAll())
    for (player in players) {
        (player as CraftPlayer).handle.connection.send(packet)
    }
}

fun Entity.sendDestroy(players: List<Player>) {
    this as CraftEntity
    val packet = ClientboundRemoveEntitiesPacket(handle.id)
    for (player in players) {
        (player as CraftPlayer).handle.connection.send(packet)
    }
}

// see CraftInventoryView#sendInventoryTitleChange(...)
fun InventoryView.sendTitleChange(title: Component) {
    require(player is Player) { "NPCs are not currently supported for this function" }
    require(topInventory.type.isCreatable) { "Only creatable inventories can have their title changed" }
    val player = (player as CraftPlayer).handle as ServerPlayer
    val containerId = player.containerMenu.containerId
    val windowType = CraftContainer.getNotchInventoryType(topInventory)
    player.connection.send(ClientboundOpenScreenPacket(containerId, windowType, PaperAdventure.asVanilla(title)))
    player.containerMenu.sendAllDataToRemote()
}

fun Player.addChannelHandler(handler: ChannelHandler) {
    this as CraftPlayer
    val pipeline = handle.connection.connection.channel.pipeline()
    pipeline.toMap().forEach {
        if (it.value is Connection) {
            pipeline.addBefore(it.key, handler.javaClass.name, handler)
            return
        }
    }
}

/**
 * 获取服务端主线程调度器 Server Thread
 * 可以避免 bukkit scheduler 执行任务的 1 tick 延迟
 */
fun Server.getServerThreadExecutor(): Executor {
    val minecraftServer = (this as CraftServer).server
    return Executor {
        if (minecraftServer.isSameThread) {
            it.run()
        } else {
            minecraftServer.execute(it)
        }
    }
}

fun CommandSourceStack.handleError(e: CommandSyntaxException) {
    this as net.minecraft.commands.CommandSourceStack
    handleError(e, false, null)
}
