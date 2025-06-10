package server.bukkit.gameplay.skin

import com.destroystokyo.paper.profile.ProfileProperty
import net.afyer.afybroker.client.Broker
import net.afyer.afybroker.core.message.PlayerProfilePropertyMessage
import server.bukkit.gameplay.player.BaseManager
import server.bukkit.gameplay.player.GamePlayer
import server.bukkit.gameplay.player.preload
import server.bukkit.util.submit
import server.common.message.database.PlayerDataRequestMessage


/**
 * @author Nipuru
 * @since 2024/11/29 15:09
 */
class SkinManager(player: GamePlayer) : BaseManager(player) {

    private lateinit var data: SkinData

    var texture: Array<String>
        get() = data.texture
        set(value) {
            data.texture = value
            player.update(data, SkinData::texture)
            player.core.updateShared = true
        }

    fun preload(request: PlayerDataRequestMessage) {
        request.preload<SkinData>()
    }

    fun unpack(dataInfo: server.bukkit.gameplay.player.DataInfo) {
        data = dataInfo.unpack<SkinData>() ?: SkinData().also { player.insert(it) }
    }

    fun pack(dataInfo: server.bukkit.gameplay.player.DataInfo) {
        dataInfo.pack(data)
    }

    fun applySkin() {
        if (texture.size == 2) {
            val property =  ProfileProperty("textures", texture[0], texture[1])
            submit {
                val request = PlayerProfilePropertyMessage()
                    .setUniqueId(player.uniqueId)
                    .update(property.name , property.value, property.signature)
                Broker.invokeSync<Any?>(request) // 更新 proxy 的信息
                submit(async = false) {
                    val profile = player.bukkitPlayer.playerProfile
                    profile.setProperty(property)
                    player.bukkitPlayer.playerProfile = profile
                }
            }
        }
    }
}
