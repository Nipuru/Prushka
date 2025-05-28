package top.nipuru.prushka.game.listener

import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.spigotmc.event.player.PlayerSpawnLocationEvent


/**
 * @author Nipuru
 * @since 2024/11/21 17:03
 */
class PlayerSpawnLocationListener(private val spawnLocations: MutableMap<String, Location>) : Listener {
    @EventHandler
    fun onEvent(e: PlayerSpawnLocationEvent) {
        val location = spawnLocations.remove(e.player.name)
        if (location != null) {
            e.spawnLocation = location
        }
    }
}
