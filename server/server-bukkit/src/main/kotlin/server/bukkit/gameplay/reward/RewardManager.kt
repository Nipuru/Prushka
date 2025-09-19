package server.bukkit.gameplay.reward

import server.bukkit.constant.PROPERTY_COIN
import server.bukkit.constant.PROPERTY_POINTS
import server.bukkit.constant.REWARD_PROPERTY
import server.bukkit.gameplay.player.*
import server.bukkit.logger.LogServer
import server.common.logger.Logger


class RewardManager(player: GamePlayer) : BaseManager(player) {
    
    private val rewards = mutableMapOf<Pair<Int, Int>, RewardData>()

    fun preload(request: TableInfos) {
        request.preload<RewardData>()
    }
    
    fun unpack(dataInfo: DataInfo) {
        for (reward in dataInfo.unpackList<RewardData>()) {
            rewards[reward.type to reward.id] = reward
        }
    }
    
    fun pack(dataInfo: DataInfo) {
        for (reward in rewards.values) {
            dataInfo.pack(reward)
        }
    }

    fun giveRewards(rewards: Array<RewardInfo>, way: Int) {
        for (reward in rewards) {
            when (reward.type) {
                REWARD_PROPERTY -> addProperty(reward.id, reward.num.toLong(), way)
                else -> addReward(reward.type, reward.id, reward.num.toLong(), way)
            }
        }
    }


    fun getPropertyAmount(id: Int): Long {
        return when (id) {
            // 部分道具使用单独字段存储 便于维护
            PROPERTY_COIN -> player.core.coin
            PROPERTY_POINTS -> player.core.points
            else -> getRewardAmount(REWARD_PROPERTY, id)
        }
    }

    fun addProperty(id: Int, amount: Long, way: Int): Boolean {
        return when (id) {
            PROPERTY_COIN -> player.core.addCoin(amount, way)
            PROPERTY_POINTS -> player.core.addPoints(amount, way)
            else -> addReward(REWARD_PROPERTY, id, amount, way)
        }
    }

    fun subtractProperty(id: Int, amount: Long, way: Int): Boolean {
        return when (id) {
            PROPERTY_COIN -> player.core.subtractCoin(amount, way)
            PROPERTY_POINTS -> player.core.subtractPoints(amount, way)
            else -> subtractReward(REWARD_PROPERTY, id, amount, way)
        }
    }

    fun checkProperties(properties: Map<Int, Int>): Boolean {
        return properties.all { (id, needAmount) ->
            needAmount == 0 || needAmount > 0 && getPropertyAmount(id) >= needAmount
        }
    }

    fun subtractProperties(properties: Map<Int, Int>, way: Int) {
        properties.forEach { (id, amount) ->
            subtractProperty(id, amount.toLong(), way)
        }
    }

    fun getRewardAmount(type: Int, id: Int): Long {
        return getReward(type, id).amount
    }

    fun addReward(type: Int, id: Int, amount: Long, way: Int): Boolean {
        if (amount == 0L) return true
        if (amount < 0L) {
            Logger.error("add invalid reward amount: {}", amount)
            return false
        }
        val reward = getReward(type, id)
        reward.amount += amount
        LogServer.logAddReward(player.playerId, type, id, amount, way)
        player.update(reward, RewardData::amount)
        return true
    }

    fun subtractReward(type: Int, id: Int, amount: Long, way: Int): Boolean {
        if (amount == 0L) return true
        if (amount < 0L) {
            Logger.error("subtract invalid reward amount: {}", amount)
            return false
        }
        val reward = getReward(type, id)
        reward.amount -= amount
        LogServer.logSubtractReward(player.playerId, type, id, amount, way)
        player.update(reward, RewardData::amount)
        return true
    }

    private fun getReward(type: Int, id: Int): RewardData {
        return rewards.getOrPut(type to id) {
            RewardData(type = type, id = id, amount =  0)
        }
    }
}
