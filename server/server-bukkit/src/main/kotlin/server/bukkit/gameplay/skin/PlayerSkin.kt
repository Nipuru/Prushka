package server.bukkit.gameplay.skin

import com.destroystokyo.paper.profile.PlayerProfile
import org.bukkit.Bukkit
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
    val image: ByteArray by lazy { url.readBytes() }

    companion object {

        /**
         * 联网获取一个玩家皮肤
         *
         * @param name 玩家名称
         * @return 玩家皮肤
         */
        fun create(name: String): CompletableFuture<PlayerSkin?> {
            return CompletableFuture.supplyAsync {
                val profile = Bukkit.createProfile(name)
                profile.complete(true, true)
                read(profile)
            }
        }

        /**
         * 读取一个玩家皮肤
         *
         * @param profile 玩家信息
         * @return 玩家皮肤
         */
        fun read(profile: PlayerProfile): PlayerSkin? {
            if (!profile.hasTextures()) return null
            val textures = profile.properties.first { it.name == "textures" }
            return PlayerSkin(profile.name!!, textures.value, textures.signature!!, profile.textures.skin!!)
        }
    }
}