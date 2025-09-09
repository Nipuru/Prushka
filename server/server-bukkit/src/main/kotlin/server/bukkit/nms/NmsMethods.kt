package server.bukkit.nms

import com.mojang.brigadier.Message
import io.netty.channel.ChannelHandler
import io.papermc.paper.adventure.PaperAdventure
import net.minecraft.network.Connection
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerLevel
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import server.bukkit.util.text.component
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
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
    val serverEntity = ServerEntity(handle.level() as ServerLevel, handle, 0, false, {}, emptySet())
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

