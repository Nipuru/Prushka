table rank {
    @key(config_id)

    config_id: int32  // 配置id
    name: ref<i18n.key>  // 名称
    name_color: string  // 聊天名字颜色
    chat_color: string  // 聊天文本颜色

    @data
    0, rank.player, #ACACAC, #D3D3D3
    1, rank.vip1, #6BD266, #A2F99D
    2, rank.vip2, #4FD2BF, #91F8EA
    3, rank.vip3, #B173E2, #E2A5FF
    7, rank.admin, #FF6565, #FFCACD
}
