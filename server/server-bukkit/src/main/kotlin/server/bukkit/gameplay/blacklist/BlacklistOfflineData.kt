package server.bukkit.gameplay.blacklist

class BlacklistOfflineData(val cmd: Int, val playerId: Int, val dbId: Int) {
    companion object {
        const val ADD: Int = 1
        const val REMOVE: Int = 2
    }
}
