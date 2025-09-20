package server.bukkit.gameplay.skin

import com.destroystokyo.paper.profile.PlayerProfile
import io.papermc.paper.datacomponent.item.ResolvableProfile
import server.bukkit.BukkitPlugin
import server.bukkit.util.completeFuture
import java.net.URL
import java.util.concurrent.CompletableFuture


/**
 * 表示一个玩家的皮肤
 *
 * @author Nipuru
 * @since 2025/08/18 13:38
 */
class PlayerSkin(
    val name: String,
    val value: String,
    val signature: String,
    val url: URL
) {

    /**
     * 皮肤图片
     * IO操作 需要异步调用
     */
    val image: CompletableFuture<ByteArray> get() {
        return BukkitPlugin.bizThread.completeFuture {
            url.readBytes()
        }
    }

    companion object {
        /**
         * 联网获取一个玩家皮肤
         *
         * @param name 玩家名称
         * @return 玩家皮肤
         */
        @Suppress("UnstableApiUsage")
        fun create(name: String): CompletableFuture<PlayerSkin?> {
            val resolver = ResolvableProfile.resolvableProfile().name(name).build()
            return resolver.resolve().thenApply { profile ->
                read(profile)
            }
        }

        /**
         * 读取一个玩家皮肤
         *
         * @param profile 玩家信息
         * @return 玩家皮肤
         */
        fun read(profile: PlayerProfile?): PlayerSkin? {
            if (profile == null || !profile.hasTextures()) return null
            val textures = profile.properties.first { it.name == "textures" }
            return PlayerSkin(profile.name!!, textures.value, textures.signature!!, profile.textures.skin!!)
        }
    }
}