package server.bukkit.nms

import net.minecraft.network.Connection
import net.minecraft.server.MinecraftServer
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.craftbukkit.util.CraftMagicNumbers
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import sun.misc.Unsafe
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

val dataVersion: Int
    get() = CraftMagicNumbers.INSTANCE.dataVersion

fun Player.hasDisconnected(): Boolean {
    val serverPlayer = (this as CraftPlayer).handle
    return serverPlayer.hasDisconnected()
}

//停止接受客户端发包
fun Player.freeze() {
    val craftPlayer = this as CraftPlayer

    val serverPlayer = craftPlayer.handle
    val connection = serverPlayer.connection.connection
    UnsafeHolder.setStopReadPacket(connection)
}

fun Player.placeBackInInventory(itemStack: ItemStack) {
    val serverPlayer = (this as CraftPlayer).handle
    serverPlayer.inventory.placeItemBackInInventory(CraftItemStack.asNMSCopy(itemStack))
}

fun Player.quit() {
    val playerList = MinecraftServer.getServer().playerList
    playerList.remove((this as CraftPlayer).handle)
}

fun Player.isFreezing(): Boolean {
    val craftPlayer = this as CraftPlayer
    val serverPlayer = craftPlayer.handle
    val connection = serverPlayer.connection.connection
    return UnsafeHolder.isStopReadPacket(connection)
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

private object UnsafeHolder {
    private val unsafe: Unsafe
    private val stopReadPacketOffset: Long

    fun setStopReadPacket(connection: Connection?) {
        unsafe.putObject(connection, stopReadPacketOffset, true)
    }

    fun isStopReadPacket(connection: Connection?): Boolean {
        return unsafe.getBoolean(connection, stopReadPacketOffset)
    }

    init {
        try {
            val unsafeField = Unsafe::class.java.getDeclaredField("theUnsafe")
            unsafeField.isAccessible = true
            unsafe = unsafeField[null] as Unsafe
            stopReadPacketOffset = unsafe.objectFieldOffset(
                Connection::class.java.getDeclaredField("stopReadingPackets")
            )
        } catch (e: Exception) {
            throw ExceptionInInitializerError(e)
        }
    }
}