package server.bukkit.nms

import com.destroystokyo.paper.profile.CraftPlayerProfile
import com.destroystokyo.paper.profile.PlayerProfile
import com.mojang.authlib.GameProfile
import com.mojang.authlib.ProfileLookupCallback
import com.mojang.brigadier.Message
import io.papermc.paper.adventure.AdventureComponent
import net.kyori.adventure.text.Component
import net.minecraft.server.MinecraftServer
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import server.bukkit.util.component
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.CompletableFuture
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

/** 将 adventure Component 转换成 brigadier Message **/
fun Component.message(): Message {
    return AdventureComponent( this)
}

fun String.message(): Message {
    return component().message()
}

/**
 * 获取一个在线玩家的游戏信息 用于获取皮肤等
 */
fun createPlayerProfile(name: String): CompletableFuture<PlayerProfile> {
    val future = CompletableFuture<PlayerProfile>()
    CompletableFuture.runAsync {
        MinecraftServer.getServer().profileRepository.findProfilesByNames(arrayOf(name), object :
            ProfileLookupCallback {
            override fun onProfileLookupSucceeded(profile: GameProfile) {
                val playerProfile = CraftPlayerProfile.asBukkitMirror(profile)
                playerProfile.complete(true,  true)
                future.complete(playerProfile)
            }

            override fun onProfileLookupFailed(profileName: String, exception: Exception) {
                future.completeExceptionally(Exception("Failed to lookup profile $profileName", exception))
            }
        })
    }
    return future
}