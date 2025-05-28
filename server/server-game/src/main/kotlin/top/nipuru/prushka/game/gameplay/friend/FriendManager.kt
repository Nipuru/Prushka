package top.nipuru.prushka.game.gameplay.friend

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import top.nipuru.prushka.common.message.database.PlayerDataRequestMessage
import top.nipuru.prushka.game.gameplay.player.BaseManager
import top.nipuru.prushka.game.gameplay.player.DataInfo
import top.nipuru.prushka.game.gameplay.player.GamePlayer
import top.nipuru.prushka.game.gameplay.player.preload
import top.nipuru.prushka.game.time.TimeManager
import top.nipuru.prushka.game.util.fromJson
import top.nipuru.prushka.game.util.toJson

private const val moduleName = "friendship"

class FriendManager(player: GamePlayer) : BaseManager(player) {
    /** 好友列表  */
    private val friendships = Int2ObjectOpenHashMap<FriendshipData>()

    /** 收到的好友请求列表  */
    private val friendRequests = Int2ObjectOpenHashMap<FriendRequest>()

    fun preload(request: PlayerDataRequestMessage) {
        request.preload(FriendshipData::class.java)
        request.preload(FriendRequest::class.java)
    }

    fun unpack(dataInfo: DataInfo) {
        dataInfo.unpackList(FriendshipData::class.java)
            .forEach{ friendships.put(it.friendId, it) }
        dataInfo.unpackList(FriendRequest::class.java)
            .forEach{ friendRequests.put(it.friendId, it) }
    }

    fun pack(dataInfo: DataInfo) {
        friendships.values.forEach(dataInfo::pack)
        friendRequests.values.forEach(dataInfo::pack)
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

    fun getFriendRequests(): Int2ObjectMap<FriendRequest> {
        return Int2ObjectOpenHashMap(friendRequests)
    }

    fun hasFriendRequest(friendId: Int): Boolean {
        return friendRequests.containsKey(friendId)
    }

    fun getFriendRequest(playerId: Int): FriendRequest? {
        return friendRequests[playerId]
    }

    fun deleteFriend(name: String, friendId: Int, friendDbId: Int) {
        val friendship = friendships.remove(friendId) ?: return
        player.delete(friendship)
        pushOfflineData(name, friendId, friendDbId, FriendshipOfflineData.DELETE, player.playerId, player.dbId, 0L)
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
        friendship = FriendshipData()
        friendship.friendId = friendId
        friendship.createTime = TimeManager.now
        friendships.put(friendId, friendship)
        player.insert(friendship)
        pushOfflineData(
            name,
            friendId,
            friendDbId,
            FriendshipOfflineData.ACCEPT,
            player.playerId,
            player.dbId,
            friendship.createTime
        )
    }

    fun addFriend(name: String, friendId: Int, friendDbId: Int) {
        if (friendships.containsKey(friendId)) return
        pushOfflineData(
            name,
            friendId,
            friendDbId,
            FriendshipOfflineData.REQUEST,
            player.playerId,
            player.dbId,
            TimeManager.now
        )
    }

    // 通知请求不要在这里处理，要单独发
    private fun handleOfflineData(json: String, isOnline: Boolean): Boolean {
        val data = json.fromJson<FriendshipOfflineData>()
        when (data.cmd) {
            FriendshipOfflineData.REQUEST -> {
                if (friendRequests.containsKey(data.friendId)) return true
                val friendRequest = FriendRequest()
                friendRequest.friendId = data.friendId
                friendRequest.createTime = data.createTime
                friendRequests.put(friendRequest.friendId, friendRequest)
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
