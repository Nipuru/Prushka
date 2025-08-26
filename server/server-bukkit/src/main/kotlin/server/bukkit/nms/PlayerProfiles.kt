package server.bukkit.nms

import com.destroystokyo.paper.profile.CraftPlayerProfile
import com.destroystokyo.paper.profile.PlayerProfile
import com.mojang.authlib.GameProfile
import com.mojang.authlib.ProfileLookupCallback
import net.minecraft.server.MinecraftServer
import java.util.concurrent.CompletableFuture


/**
 * 玩家信息相关
 * @author Nipuru
 * @since 2025/08/26 22:36
 */
object PlayerProfiles {
    /**
     * 获取一个在线玩家的游戏信息 用于获取皮肤等
     */
    fun completeOnline(name: String): CompletableFuture<PlayerProfile> {
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
}