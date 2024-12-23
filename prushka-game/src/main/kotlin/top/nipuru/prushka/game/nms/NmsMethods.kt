package top.nipuru.prushka.game.nms

import com.mojang.serialization.Dynamic
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtOps
import net.minecraft.network.Connection
import net.minecraft.server.MinecraftServer
import net.minecraft.util.datafix.fixes.References
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_20_R3.persistence.CraftPersistentDataContainer
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.potion.PotionEffect
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import sun.misc.Unsafe
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
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

fun PersistentDataContainer.serialize(): ByteArray {
    val craftPersistentDataContainer = this as CraftPersistentDataContainer
    val compoundTag = craftPersistentDataContainer.toTagCompound()
    val baos = ByteArrayOutputStream()
    val gzip = GZIPOutputStream(baos)
    val dos = DataOutputStream(gzip)
    compoundTag.write(dos)
    dos.close()
    return baos.toByteArray()
}

fun PersistentDataContainer.clear() {
    val craftPersistentDataContainer = this as CraftPersistentDataContainer
    craftPersistentDataContainer.clear()
}

fun PersistentDataContainer.deserialize(data: ByteArray) {
    val craftPersistentDataContainer = this as CraftPersistentDataContainer
    craftPersistentDataContainer.clear()
    val bais = ByteArrayInputStream(data)
    val gzip = GZIPInputStream(bais)
    val dis = DataInputStream(gzip)
    val compoundTag = CompoundTag.TYPE.load(dis, NbtAccounter.unlimitedHeap())
    craftPersistentDataContainer.putAll(compoundTag)
    dis.close()
}

fun Array<ItemStack?>.serialize(): ByteArray {
    val baos = ByteArrayOutputStream()
    val gzip = GZIPOutputStream(baos)
    val dos = DataOutputStream(gzip)
    val listTag = ListTag()
    for (itemStack in this) {
        val compoundTag = CompoundTag()
        if (itemStack != null) {
            CraftItemStack.asNMSCopy(itemStack).save(compoundTag)
            compoundTag.putInt("dataVersion", dataVersion)
        }
        listTag.add(compoundTag)
    }
    listTag.write(dos)
    dos.close()
    return baos.toByteArray()
}

fun ByteArray.deserializeItemStacks(): Array<ItemStack?> {
    val bais = ByteArrayInputStream(this)
    val gzip = GZIPInputStream(bais)
    val dis = DataInputStream(gzip)
    val listTag = ListTag.TYPE.load(dis, NbtAccounter.unlimitedHeap())
    val itemStacks = arrayOfNulls<ItemStack>(listTag.size)
    for (i in listTag.indices) {
        var compoundTag = listTag[i] as CompoundTag
        if (!compoundTag.isEmpty) {
            val version = compoundTag.getInt("dataVersion")
            if (version < dataVersion) {
                compoundTag = MinecraftServer.getServer().fixerUpper.update(
                    References.ITEM_STACK, Dynamic(NbtOps.INSTANCE, compoundTag), version,
                    dataVersion
                ).value as CompoundTag
            }
            itemStacks[i] = CraftItemStack.asCraftMirror(net.minecraft.world.item.ItemStack.of(compoundTag))
        }
    }
    dis.close()
    return itemStacks
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