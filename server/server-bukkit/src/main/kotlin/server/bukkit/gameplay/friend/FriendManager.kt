package server.bukkit.gameplay.friend

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import server.bukkit.constant.OFFLINE_FRIEND_ACCEPT
import server.bukkit.constant.OFFLINE_FRIEND_DELETE
import server.bukkit.constant.OFFLINE_FRIEND_REQUEST
import server.bukkit.gameplay.player.*
import server.bukkit.time.TimeManager
import server.common.util.fromJson
import server.common.util.toJson


class FriendManager(player: GamePlayer) : BaseManager(player) {

    /** 好友列表  */
    private val friendships = Int2ObjectOpenHashMap<FriendshipData>()

    /** 收到的好友请求列表  */
    private val friendRequests = Int2ObjectOpenHashMap<FriendRequestData>()

    fun preload(request: TableInfos) {
        request.preload<FriendshipData>()
        request.preload<FriendRequestData>()
    }

    fun unpack(dataInfo: DataInfo) {
        dataInfo.unpackList<FriendshipData>()
            .forEach{ friendships.put(it.friendId, it) }
        dataInfo.unpackList<FriendRequestData>()
            .forEach{ friendRequests.put(it.friendId, it) }
    }

    fun pack(dataInfo: DataInfo) {
        friendships.values.forEach(dataInfo::pack)
        friendRequests.values.forEach(dataInfo::pack)
    }

    fun init() {
        player.offline.registerHandler(OFFLINE_FRIEND_REQUEST, this::handleFriendRequest)
        player.offline.registerHandler(OFFLINE_FRIEND_ACCEPT, this::handleFriendAccept)
        player.offline.registerHandler(OFFLINE_FRIEND_DELETE, this::handleFriendDelete)
    }

    fun getFriends(): Int2ObjectMap<FriendshipData> {
        return Int2ObjectOpenHashMap(friendships)
    }

    fun isFriend(friendId: Int): Boolean {
        return friendships.containsKey(friendId)
    }

    fun getFriend(friendId: Int): FriendshipData? {
        return friendships[friendId]
    }

    val friendCount: Int
        get() = friendships.count()

    fun getFriendRequests(): Int2ObjectMap<FriendRequestData> {
        return friendRequests
    }

    fun hasFriendRequest(friendId: Int): Boolean {
        return friendRequests.containsKey(friendId)
    }

    fun getFriendRequest(playerId: Int): FriendRequestData? {
        return friendRequests[playerId]
    }

    fun deleteFriend(name: String, friendId: Int, friendDbId: Int) {
        val friendship = friendships.remove(friendId) ?: return
        player.delete(friendship)
        player.offline.pushOfflineData(name, friendId, friendDbId, OFFLINE_FRIEND_DELETE, createOfflineData(), friendId.toString())
    }

    fun rejectFriend(friendId: Int) {
        val friendRequest = friendRequests.remove(friendId) ?: return
        player.delete(friendRequest)
    }

    fun acceptFriend(name: String, friendId: Int, friendDbId: Int) {
        val friendRequest = friendRequests.remove(friendId) ?: return
        player.delete(friendRequest)
        var friendship = friendships[friendId]
        if (friendship != null) return
        val now = TimeManager.now
        friendship = FriendshipData()
        friendship.friendId = friendId
        friendship.createTime = now
        friendships.put(friendId, friendship)
        player.insert(friendship)
        player.offline.pushOfflineData(name, friendId, friendDbId, OFFLINE_FRIEND_ACCEPT, createOfflineData(), friendId.toString())
    }

    fun requestFriend(name: String, friendId: Int, friendDbId: Int) {
        if (friendships.containsKey(friendId)) return
        player.offline.pushOfflineData(name, friendId, friendDbId, OFFLINE_FRIEND_REQUEST, createOfflineData(), friendId.toString())
    }

    /** 强制建立好友关系 */
    fun addFriendDirectly(name: String, friendId: Int, friendDbId: Int) {
        val now = TimeManager.now
        if (!friendships.containsKey(friendId)) {
            val friendship = FriendshipData()
            friendship.friendId = friendId
            friendship.createTime = now
            friendships.put(friendId, friendship)
            player.insert(friendship)
        }
        player.offline.pushOfflineData(name, friendId, friendDbId, OFFLINE_FRIEND_ACCEPT, createOfflineData(), friendId.toString())
    }

    private fun createOfflineData(): String {
        val offlineData = FriendshipOfflineData(player.playerId, TimeManager.now)
        return offlineData.toJson()
    }

    private fun handleFriendRequest(json: String, isOnline: Boolean) {
        val data = json.fromJson<FriendshipOfflineData>()
        if (friendRequests.containsKey(data.friendId)) return
        val friendRequest = FriendRequestData()
        friendRequest.friendId = data.friendId
        friendRequest.createTime = data.createTime
        friendRequests.put(friendRequest.friendId, friendRequest)
        player.insert(friendRequest)
    }

    private fun handleFriendAccept(json: String, isOnline: Boolean) {
        val data = json.fromJson<FriendshipOfflineData>()
        var friendship = friendships[data.friendId]
        if (friendship != null) return
        friendship = FriendshipData()
        friendship.friendId = data.friendId
        friendship.createTime = data.createTime
        friendships.put(friendship.friendId, friendship)
        player.insert(friendship)
    }

    private fun handleFriendDelete(json: String, isOnline: Boolean) {
        val data = json.fromJson<FriendshipOfflineData>()
        val friendship = friendships.remove(data.friendId) ?: return
        player.delete(friendship)
    }
}
