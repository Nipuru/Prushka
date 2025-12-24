table property {  // 道具表
    @key(config_id)

    config_id: int32  // 配置id
    name: ref<i18n.key>  // 名称

    @data
    1, property.gold
    2, property.points
}
