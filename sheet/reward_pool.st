table reward_pool {  // 奖励池表
    @akey(pool_id)

    pool_id: int32  // 配置id
    type: int32  // 类型
    id: int32  // 物品id
    amount: int32  // 数量
    weight: int32  // 权重
}
