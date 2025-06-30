package server.bukkit.gameplay.player

import server.bukkit.constant.Items
import server.common.sheet.Sheet
import server.common.sheet.getStReward
import server.common.sheet.getStRewardPools
import server.common.sheet.getStRewards


/**
 * @author Nipuru
 * @since 2024/11/14 09:38
 */
class RewardInfo(
    val type: Int,
    val id: Int,
    val num: Int,
)

fun getRewards(rewardId: Int) : List<RewardInfo> {
    val cfgs = Sheet.getStRewards(rewardId)
    val rewards = mutableListOf<RewardInfo>()
    for (cfg in cfgs) {
        if (cfg.type == Items.ITEM_POOL) {
            for (i in 1..cfg.amount) {
                rewards.add(getRewardByPool(cfg.rewardId))
            }
        } else {
            rewards.add(RewardInfo(cfg.type, cfg.rewardId, cfg.amount))
        }
    }
    return rewards
}

fun getRewardByPool(poolId: Int) : RewardInfo {
    val cfgs = Sheet.getStRewardPools(poolId)
    val totalWeight = cfgs.sumOf { it.weight }
    val rd = Math.random() * totalWeight
    var weight = 0.0
    for (cfg in cfgs) {
        weight += cfg.weight
        if (rd < weight) {
            return RewardInfo(cfg.type, cfg.id, cfg.amount)
        }
    }
    return null!!
}

fun List<RewardInfo>.merge(): Array<RewardInfo> {
    return this.groupBy { it.type to it.id }
        .map { (key, rewards) -> RewardInfo(key.first, key.second, rewards.sumOf { it.num }) }
        .toTypedArray()
}

