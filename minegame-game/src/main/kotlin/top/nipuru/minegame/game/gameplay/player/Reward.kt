package top.nipuru.minegame.game.gameplay.player

import top.nipuru.minegame.common.sheet.stRewardAMap
import top.nipuru.minegame.common.sheet.stRewardPoolAMap
import top.nipuru.minegame.game.constants.Items


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
    var cfgs = stRewardAMap[rewardId]!!
    val rewards = mutableListOf<RewardInfo>()
    for (cfg in cfgs) {
        if (cfg.type == Items.ITEM_POOL) {
            for (i in 1..cfg.num) {
                rewards.add(getRewardByPool(cfg.rewardId))
            }
        } else {
            rewards.add(RewardInfo(cfg.type, cfg.rewardId, cfg.num))
        }
    }
    return rewards
}

fun getRewardByPool(poolId: Int) : RewardInfo {
    val cfgs = stRewardPoolAMap[poolId]!!
    val totalWeight = cfgs.sumOf { it.weight }
    val rd = Math.random() * totalWeight
    var weight = 0.0
    for (cfg in cfgs) {
        weight += cfg.weight
        if (rd < weight) {
            return RewardInfo(cfg.type, cfg.id, cfg.num)
        }
    }
    return null!!
}

fun List<RewardInfo>.merge(): Array<RewardInfo> {
    return this.groupBy { it.type to it.id }
        .map { (key, rewards) -> RewardInfo(key.first, key.second, rewards.sumOf { it.num }) }
        .toTypedArray()
}

