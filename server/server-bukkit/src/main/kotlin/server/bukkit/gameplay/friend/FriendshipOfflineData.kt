package server.bukkit.gameplay.friend

class FriendshipOfflineData(val cmd: Int, val friendId: Int, val friendDbId: Int, val createTime: Long) {
    companion object {
        const val REQUEST: Int = 1
        const val ACCEPT: Int = 2
        const val DELETE: Int = 4
    }
}
