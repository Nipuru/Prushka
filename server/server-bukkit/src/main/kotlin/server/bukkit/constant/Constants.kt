package server.bukkit.constant


/**
 * 一些全局常量
 */

// 时间戳
const val SECOND: Long = 1000
const val MINUTE: Long = SECOND * 60
const val HOUR: Long = MINUTE * 60
const val DAY: Long = HOUR * 24
const val WEEK: Long = DAY * 7
const val MONTH: Long = DAY * 30
const val YEAR: Long = DAY * 365

// 奖励类型 st_reward
const val REWARD_POOL: Int = 1        // 奖池
const val REWARD_PROPERTY: Int = 2    // 虚拟道具

// 虚拟道具 st_property
const val PROPERTY_COIN: Int = 1
const val PROPERTY_POINTS: Int = 2

// 离线数据模块名
const val OFFLINE_FRIEND_REQUEST = "friend_request"
const val OFFLINE_FRIEND_ACCEPT = "friend_accept"
const val OFFLINE_FRIEND_DELETE = "friend_delete"
const val OFFLINE_BLACKLIST_ADD = "blacklist_add"
const val OFFLINE_BLACKLIST_REMOVE = "blacklist_remove"


    