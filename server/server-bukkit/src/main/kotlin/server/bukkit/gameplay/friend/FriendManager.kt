package server.bukkit.gameplay.friend

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import server.bukkit.gameplay.player.*
import server.bukkit.time.TimeManager
import server.bukkit.util.fromJson
import server.bukkit.util.toJson


class FriendManager(player: GamePlayer) : BaseManager(player) {

    private val moduleName = "friendship"

    /** 好友列表  */
    private val friendships = Int2ObjectOpenHashMap<FriendshipData>()

    /** 收到的好友请求列表  */
    private val inboundFriendRequests = Int2ObjectOpenHashMap<FriendRequestInboundData>()

    /** 发出的好友请求（防止重复发送离线消息） */
    private val outboundFriendRequests = Int2ObjectOpenHashMap<FriendRequestOutboundData>()

    fun preload(request: TableInfos) {
        request.preload<FriendshipData>()
        request.preload<FriendRequestInboundData>()
        request.preload<FriendRequestOutboundData>()
    }

    fun unpack(dataInfo: DataInfo) {
        dataInfo.unpackList<FriendshipData>()
            .forEach{ friendships.put(it.friendId, it) }
        dataInfo.unpackList<FriendRequestInboundData>()
            .forEach{ inboundFriendRequests.put(it.friendId, it) }
        dataInfo.unpackList<FriendRequestOutboundData>()
            .forEach{ outboundFriendRequests.put(it.friendId, it) }
    }

    fun pack(dataInfo: DataInfo) {
        friendships.values.forEach(dataInfo::pack)
        inboundFriendRequests.values.forEach(dataInfo::pack)
        outboundFriendRequests.values.forEach(dataInfo::pack)
    }

    fun init() {
        player.offline.registerHandler(moduleName, this::handleOfflineData)
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

    fun getReceivedFriendRequests(): Int2ObjectMap<FriendRequestInboundData> {
        return inboundFriendRequests
    }

    fun hasReceivedFriendRequest(friendId: Int): Boolean {
        return inboundFriendRequests.containsKey(friendId)
    }

    fun getReceivedFriendRequest(playerId: Int): FriendRequestInboundData? {
        return inboundFriendRequests[playerId]
    }

    fun deleteFriend(name: String, friendId: Int, friendDbId: Int) {
        val friendship = friendships.remove(friendId) ?: return
        player.delete(friendship)
        pushOfflineData(name, friendId, friendDbId, FriendshipOfflineData.DELETE, player.playerId, player.dbId, 0L)
    }

    fun rejectFriend(name: String, friendId: Int, friendDbId: Int) {
        val friendRequest = inboundFriendRequests.remove(friendId) ?: return
        player.delete(friendRequest)
        pushOfflineData(name, friendId, friendDbId, FriendshipOfflineData.REJECT, player.playerId, player.dbId, 0L)
    }

    fun acceptFriend(name: String, friendId: Int, friendDbId: Int) {
        val friendRequest = inboundFriendRequests.remove(friendId) ?: return
        player.delete(friendRequest)
        var friendship = friendships[friendId]
        if (friendship != null) return
        val now = TimeManager.now
        friendship = FriendshipData()
        friendship.friendId = friendId
        friendship.createTime = now
        friendships.put(friendId, friendship)
        player.insert(friendship)
        pushOfflineData(name, friendId, friendDbId, FriendshipOfflineData.ACCEPT, player.playerId, player.dbId, now)
    }

    fun requestFriend(name: String, friendId: Int, friendDbId: Int) {
        if (friendships.containsKey(friendId)) return
        if (outboundFriendRequests.containsKey(friendId)) return
        val now = TimeManager.now
        val friendRequest = FriendRequestOutboundData()
        friendRequest.friendId = friendId
        outboundFriendRequests.put(friendId, friendRequest)
        player.insert(friendRequest)
        pushOfflineData(name, friendId, friendDbId, FriendshipOfflineData.REQUEST, player.playerId, player.dbId, now)
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
        pushOfflineData(name, friendId, friendDbId, FriendshipOfflineData.ACCEPT, player.playerId, player.dbId, now)
    }

    // 通知请求不要在这里处理，要单独发
    private fun handleOfflineData(json: String, isOnline: Boolean): Boolean {
        val data = json.fromJson<FriendshipOfflineData>()
        when (data.cmd) {
            FriendshipOfflineData.REQUEST -> {
                if (inboundFriendRequests.containsKey(data.friendId)) return true
                val friendRequest = FriendRequestInboundData()
                friendRequest.friendId = data.friendId
                friendRequest.createTime = data.createTime
                inboundFriendRequests.put(friendRequest.friendId, friendRequest)
                player.insert(friendRequest)
            }

            FriendshipOfflineData.ACCEPT -> {
                var friendship = friendships[data.friendId]
                if (friendship != null) return true
                friendship = FriendshipData()
                friendship.friendId = data.friendId
                friendship.createTime = data.createTime
                friendships.put(friendship.friendId, friendship)
                player.insert(friendship)
            }

            FriendshipOfflineData.REJECT -> {
                val friendRequest = outboundFriendRequests.remove(data.friendId) ?: return true
                player.delete(friendRequest)
            }

            FriendshipOfflineData.DELETE -> {
                val friendship = friendships.remove(data.friendId) ?: return true
                player.delete(friendship)
            }

            else -> {
                return false
            }
        }
        return true
    }

    private fun pushOfflineData(name: String, friendId: Int, friendDbId: Int, cmd: Int, playerId: Int, dbId: Int, createTime: Long) {
        val offlineData = FriendshipOfflineData(cmd, playerId, dbId, createTime)
        val json = offlineData.toJson()
        player.offline.pushOfflineData(name, friendId, friendDbId, moduleName, json)
    }
}
